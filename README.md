# Cats Effects

## Referential transparency

Referential transparency is a property of a function that allows it to be replaced by its equivalent output. In simpler terms, if you call the function a second time with the same arguments, you’re guaranteed to get the same returning value.

### Type class

A type class is a type system construct that supports ad hoc polymorphism. This is achieved by adding constraints to type variables in parametrically polymorphic types. Such a constraint typically involves a type class T and a type variable a, and means that a can only be instantiated to a type whose members support the overloaded operations associated with T

### Type Alias

A type alias is usually used to simplify declaration for complex types, such as parameterized types or function types.


### Variances

class Foo[+A] // A covariant class (only the A and super class could be here)
class Bar[-A] // A contravariant class (only A and subclasses could be here)
class Baz[A]  // An invariant class (only A type allowed to be here)

## IO Monad

* thunk - a function that not yet executed

### Error Handling

