# Type classes

## Sync
// TODO check
- delay wraps a computation
- blocking wraps a computation and mark it as possibly blocking

## MonadCancel

- bracket: acquire, use, release

## Concurrent

- GenConcurrent trait
- ref - wraps a value in a Ref
- deferred - wraps a value in a Deferred
- memoize - // TODO refresh memory

## Spawn

- GenSpawn trait
- race - takes two tasks and returns the first one to finish

## Async

- async_ - stores the behaviour for both case scenarios: when the task is successful and when it fails
- executionContext - returns the execution context of the task
- evalOn - evaluates the task on a different execution context

## GenTemporal

- sleep - sleeps for a given duration
- timeoutTo - sets the implicit timer and the result type

## Clock

- monotonic
- realTime

## Parallel[M[_]]

// TODO check Monad laws
- type F[_] => F is an applicative; supports parallel evaluation
- sequential: F ~> M => a way to go from an applicative to a monad
- parallel: M ~> F => a way to go from a monad to an applicative

### object Parallel
- parSequence - takes a list of tasks and runs them in parallel
- parTraverse - takes a list of tasks and runs them in parallel

## Tagless final pattern

- When yoy have an API that is polymorphic in the effect type, you can use the tagless final pattern to abstract over the effect type by wrapping the result type in F[_](called 
  as algebra) and then specify constraint via type class:
...example...
