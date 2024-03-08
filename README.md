# Cats Effects

## Referential transparency

Referential transparency is a property of a function that allows it to be replaced by its equivalent output. In simpler terms, if you call the function a second time with the same arguments, you’re guaranteed to get the same returning value.

### Type class

- A type class is a type system construct that supports ad hoc polymorphism. This is achieved by adding constraints to type variables in parametrically polymorphic types. Such a 
constraint typically involves a type class T and a type variable a, and means that a can only be instantiated to a type whose members support the overloaded operations associated with T
- implicit methods with non-implicit parameters form a different Scala pa􏰁ern called an implicit conversion.

#### Type Class Interfaces
A type class interface is any func􏰀onality we expose to users. Interfaces are generic methods that accept instances of the type class as implicit parameters.
There are two common ways of specifying an interface: Interface Objects and Interface Syntax.

### Type Alias

A type alias is usually used to simplify declaration for complex types, such as parameterized types or function types.


### Variances

class Foo[+A] // A covariant class (only the A and super class could be here)
class Bar[-A] // A contravariant class (only A and subclasses could be here)
class Baz[A]  // An invariant class (only A type allowed to be here)

## IO Monad

* thunk - a function that not yet executed

### Concurrency

* IO does not support parallelism, however, it does contain class Par which is designed for such computation
* The execution order of parallel tasks is non-deterministic
* The Parallel typeclass from the Cats library (not Cats Effect) captures the concept of translating between two related data types.
* 

