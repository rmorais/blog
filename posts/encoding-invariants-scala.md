---
title: "Encoding Invariants in Scala"
date: 2019-06-30T11:00:16+01:00
lastmod: 2019-06-30T11:00:16+01:00
draft: false
keywords: []
description: ""
tags: [Scala, Invariants, Type Safety]
categories: []
author: ""

# You can also close(false) or open(true) something for this content.
# P.S. comment can only be closed
comment: false
toc: true
autoCollapseToc: false
# You can also define another contentCopyright. e.g. contentCopyright: "This is another copyright."
contentCopyright: false
reward: false
mathjax: false
---

<!--more-->

## Motivation

Scala is a statically type language with a very powerful type system. We can take advantage of this and let the compiler make our programs more robust.

I want to show how we can take advantage of this and explore how we can use the compiler to help us create more robust types which can't have invalid values.

Let's say we want to encode a `Counter`. We can use a case class to encode it.

```scala mdoc
case class Counter(value: Int)
```

Now imagine that we want our counter to have only positive values. There is another requirement that non positive values should default to the initial value of `1`.
There are a couple of ways we can achieve this which I will explore below.

This example is a bit contrived but will suffice for the purpose of our demonstration.

## Using preconditions

Scala provides a set of preconditions that we can use to validate our data, being one of them `require`.
We can use it in the following way.

```scala mdoc:reset
case class Counter(value: Int) {
    require(value > 0)
}
```

This will make sure that we can't create an instance of `Counter` that doesn't respect the restriction, as we can see bellow.

```scala mdoc:crash

Counter(-1)

```

As the value provided doesn't respect the pre condition `require` will throw an exception.
While this partially solves our problem, it isn't a good solution.

One issue is that it will fail at runtime whenever we create an instance with a negative value. The other problem is that users of this class don't know that it can fail. This can lead to unexpected errors while running the application.

In this case we are not really using the compiler in our favour.
Let's see if we can leverage the scala type system and lift this restriction into the type level.

## Making the constructor private

Ideally, we would like to have a single mechanism to allow the creation of valid instances and make the user aware that it must deal with the possibility of failure.

To achieve this, we can mark the constructor of the case class private and provide a custom constructor that will do the required validation. This is usually called a smart constructor.
Let's see how it looks like.

```scala mdoc:reset

final case class Counter private (value: Int)

object Counter {
    def fromInt(value: Int): Counter = if (value > 0) Counter(value) else Counter(1)
}
```

```scala mdoc:fail
new Counter(20)
```

As you can see, we can no longer use the default constructor. 
Now that we have define our own constructor, we can create valid instances.

```scala mdoc
Counter.fromInt(-3)
```

The custom constructor will always return a valid instance for our counter.
We are no longer relying on runtime validation but are instead relying on compile time validation.

Given that we are using a case class, the compiler will generate some synthetic methods for us. Generally, these are quite handy but for our case they are causing some harm.

`apply` and `copy` are two of the synthetic methods generated. And they can be used to bypass our smart constructor as we can see below.

```scala mdoc
val c = Counter.apply(-5)

val c1 = Counter(10).copy(-5)

```

To fix this, we need to tweak our smart constructor. We will need to define our own `apply` and `copy` to supress the synthetic ones.

```scala mdoc:reset
final case class Counter private (value: Int) {
    def copy(number: Int = value): Counter = Counter.fromInt(number)
}

object Counter {
    def apply(value: Int): Counter = fromInt(value)

    def fromInt(value: Int): Counter = 
        if (value > 0) new Counter(value) else new Counter(1)
}
```

Now if we try to use again the `apply` or the `copy` methods, they will just delegate to the smart constructor.

```scala mdoc
val c = Counter.apply(-5)

val c1 = Counter(10).copy(-5)
``` 

This solution thicks all the boxes for our problem. The downside is that we have to remember to suppress the synthetic methods and this only works from scala `2.12.2+`.

## Using sealed abstract case classes

The last alternative I want to explore is to use an abstract class together with anonynous subclassing. It looks like this:

```scala mdoc:reset

sealed abstract case class Counter private (value: Int)

object Counter {
    def fromInt(value: Int): Counter =
        if (value > 0) new Counter(value) {} else new Counter(1) {}
}
```

We define our counter as a `sealed` class, to limit the scope in which subclasses can be defined. In this case the scope is limited to the source file where our counter is defined.
And by defining our case class `abstract`, we disable the synthetic methods `apply` and `copy` provided by the compiler. It cannot generate this methods because there is no default constructor.

The only possible way to create an instance of our counter is through the use of our smart constructor.

As we can see below, the synthetic methods are not available, reducing the ways of bypassing our smart constructor.

```scala mdoc:fail
val c1 = Counter.apply(-5)

val c2 = Counter.fromInt(3).copy(-5)

```

This last solution is more safe because the compiler can't generate `apply` and `copy` methods. The downside is that we have to define a `sealed abstract` class plus anonymous subtyping instead of just a `final` class.

I prefer this last option mainly because I don't have to remember to supress the shyntetic methods generated by the compiler.

I think both alternatives works well and there are no clear advantages or disdvantages of using one over the other. It's more of a style preference and I find myself enforcing invariants by using primarly `sealed abstract` classes.

[refined]: https://github.com/fthomas/refined