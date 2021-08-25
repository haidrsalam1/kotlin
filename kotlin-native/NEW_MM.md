# New memory model migration guide

## Overview

**NOTE**: The new MM is still in an experimental stage. It's not production-ready.

In the new MM we are lifting restrictions placed on object sharing: there no need to freeze objects to share them
between threads.

In particular:
* Top level properties can be accessed and modified by any thread without the need to use `@SharedImmutable`.
* Objects passing through interop can be accessed and modified by any thread without the need to freeze them.
* `Worker.executeAfter` will no longer require `operation` to be frozen, and `Worker.execute` will no longer require
  `producer` to return an isolated object subgraph.

A few caveats:
* As with the previous MM, memory is not reclaimed eagerly: an object is reclaimed only when GC happens. This extends
  to Swift/ObjC objects that crossed interop boundary into K/N.
* `AtomicReference` from `kotlin.native.concurrent` still requires freezing the `value`. `FreezableAtomicReference`
  can be used instead, or, alternatively, `AtomicRef` from `atomicfu` can be used (note, that the library has not reached 1.x yet).
* `deinit` on Swift/ObjC objects (and the objects referred by them) will be called on a different thread if these objects
  cross interop boundary into K/N.
* When calling Kotlin suspend functions from Swift, completion handlers might be called not on the main thread.

Together with the new MM we are bringing in another set of changes:
* Global properties are initialized lazily, when the file they are defined in is first accessed.
  This is in line with K/JVM. Properties that must be initialized at the program start can be marked with `@EagerInitialization`
  (please, consult the docs for `@EagerInitialization` before using).
* `by lazy {}` properties support thread safety modes and do not handle unbounded recursion. This is in line with K/JVM.
* Exceptions escaping `operation` in `Worker.executeAfter` are processed like in other parts of the runtime:
  by trying to execute a user-defined unhandled exception hook, or terminating the program if the hook was not found or
  failed with exception itself.

## Enable the new MM

**NOTE**: The new MM is still in an experimental stage. It's not production-ready.

Add compilation flag `-Xbinary=memoryModel=experimental`.

Alternatively, with gradle you can add

```kotlin
kotlin.targets.withType(KotlinNativeTarget::class.java) {
    binaries.all {
        binaryOptions["memoryModel"] = "experimental"
    }
}
```
to `build.gradle.kts` if you're using kotlin DSL, or
```groovy
kotlin.targets.withType(KotlinNativeTarget) {
    binaries.all {
        binaryOptions["memoryModel"] = "experimental"
    }
}
```
to `build.gradle` if you're using groovy DSL, or even
```properties
kotlin.native.binary.memoryModel=experimental
```
to `gradle.properties`.

To fully take advantage of the new MM newer versions of certain library should be used. For example (**TODO**: update versions):
* `kotlinx.coroutines`: `1.5.1-new-mm-dev1`
* `ktor`: **TODO**
Older versions (including `native-mt` for `kotlinx.coroutines`) could still be used, and will work just like with the previous MM.

## Performance issues

For the first preview we are using the simplest scheme for garbage collection: single-threaded stop-the-world
mark-and-sweep algorithm, which is triggered after enough functions and loop iterations were executed. This greatly hinders
the performance. One of our top priorities is addressing performance problems.

We don't yet have nice instruments to monitor the GC performance, so for now diagnosing most of these problems requires looking at GC logs.
To enable the logs we need to pass `-Xruntime-logs=gc=info` to the compiler. Or, in gradle:
```kotlin
kotlin.targets.withType(KotlinNativeTarget::class.java) {
    binaries.all {
        freeCompilerArgs += "-Xruntime-logs=gc=info"
    }
}
```
to `build.gradle.kts` in case of kotlin DSL, or
```groovy
kotlin.targets.withType(KotlinNativeTarget) {
    binaries.all {
        freeCompilerArgs += "-Xruntime-logs=gc=info"
    }
}
```
to `build.gradle` in case of groovy DSL.

Currently, the logs are only printed to stderr. Also, the exact contents of the logs is subject to change.

A number of known performance issues:
* Since the collector is single-threaded stop-the-world, the pause time of every thread linearly depends on the number of
  objects in the heap. The more objects that are kept alive, the longer pauses will be. Large pauses on the main thread
  can result in laggy UI event handling. Both the pause time and the amount of objects in the heap are printed to the logs for each
  cycle of GC.
* Being stop-the-world also means that all threads with K/N runtime initialized on them need to synchronize at the same
  time in order for the collection to begin. This also affects the pause time.
* There is a complicated relationship between Swift/ObjC objects and their K/N counterparts, that causes Swift/ObjC objects
  to linger longer than necessary, which means that their K/N counterparts are kept in the heap for longer, contributing
  to the slower collection time. This typically doesn't happen, but in some corner cases, for example, when
  there's a long loop that on each iteration creates a number of temporary objects that cross the Swift/ObjC
  interop boundary (e.g. calling a kotlin callback from a loop in swift or vice versa).
  In the logs there's a number of stable refs in the root set. If this number keeps growing, it may indicate that Swift/ObjC objects
  are not being freed when they should.
  **TODO**: Mitigating
* Our GC triggers do not adapt to the workload: collection may be requested far more frequently than necessary, which means
  that GC time may dominate actually useful application run time and pause the threads more frequently than needed.
  This manifests in time between cycles being close (or even less) than the pause time. Both of these numbers are printed
  in the logs. Try increasing `kotlin.native.internal.GC.threshold` and `kotlin.native.internal.GC.thresholdAllocations` to force GC
  to happen less often. Note that, the exact semantics of `threshold` and `thresholdAllocations` may change in the future.
* Freezing is currently implemented suboptimally: internally a separate memory allocation may occur for each frozen object
  (this recursively includes the object subgraph), which puts unnecessary pressure on the heap.
* Unterminated `Worker`s and unconsumed `Future`s have objects pinned to the heap, which contributes to the pause time.
  Just like Swift/ObjC interop, this also manifests in a growing number of stable refs in the root set. To mitigate, look for
  `Worker.execute` methods being called with the resulting `Future` never being consumed (via `Future.consume` or `Future.result`) and
  make sure to either consume the `Future` or replace calls with `Worker.executeAfter` instead. Also look for `Worker`s that were
  `Worker.start`ed, but were never stopped via `Worker.requestTermination()` (also note that this call also returns a `Future`).
  And finally, make sure that `execute` and `executeAfter` is only called on `Worker`s that were `Worker.start`ed or if the receiving
  worker manually processes events with `Worker.processQueue`.

## Known bugs

* Compiler caches are not supported, so compilation of debug binaries will be slower.
* Freezing machinery is not thread-safe: if an object is being frozen on one thread, and its subgraph is being modified
  on another, by the end the object will be frozen, but some subgraph of it might be not.
* Documentation is not updated to reflect changes for the new MM.
* There's no handling of application state on iOS: if application goes into the background, the collector will not be
  throttled down; on the other hand the collection is not force upon going into the background, which leaves
  the application with a larger memory footprint than necessary, making it a more likely target to be terminated by the OS.
* WASM (or indeed any target that does not have pthreads) is not supported with the new MM.

**TODO**: A place to submit feedback
