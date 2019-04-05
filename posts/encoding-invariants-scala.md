---
title: "Encoding Invariants Scala"
date: 2019-04-05T11:00:16+01:00
lastmod: 2019-04-05T11:00:16+01:00
draft: true
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
Scala is a statically type language with a very powerful type systems. We can take advantage of this and let the compiler make our programs more robust.

I want to take advantage of this and explore how we can use the compiler to help us create more robust types which can't have invalid values.

Let's say we want to encode a `Counter`. We can use a case class to encode it.

```scala mdoc
case class Counter(value: Int)
```

Now imagine that we want our counter use only positive values. There is another requirement that non positive values should default to the initial value of `1`.
There are a couple of ways we can achieve this which I will explore below.

This example is a bit contrived but will suffice for our purpose.

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
While this solves our problem, it's not a good solution. 

The main issue is that it will fail at runtime. Also the user of this class doesn't know that creating an instance can fail. This can lead to unexpected errors while running the application.

In this case we are not really using the compiler in our favour.
Let's see if we can leverage the scala type system and lift this restriction into the type level.

## Making the constructor private

Ideally, we would like to have a single mechanism to allow the creation of valid instances and make the user aware that it must deal with the possibility of failure.

To achieve this, we can mark the constructor of the case class private and provide a custom constructor that will do the required validation. This is usually called a smart constructor.
Let's see how it looks like.

```scala mdoc:reset

case class Counter private (value: Int)

object Counter {
    def fromInt(value: Int): Counter = if (value > 0) Counter(value) else Counter(1)
}
```

```scala mdoc:fail
new Counter(20) {}
```

As you can see, we can no longer use the default constructor. 
Now that we have define our own constructor, we can create valid instances.
```scala mdoc
Counter.fromInt(-3)
```
The custom constructor will always return a valid instance for our counter. 
We are no longer relying on runtime validation but are instead relying on the compile time validation.

Given that we are using a case class, the compiler will generate some synthetic methods for us. Generally, these are quite handy but for our case they are causing some harm.

`apply` or `copy` are two of the synthetic methods generated. And they can be used to bypass our smart constructor as we can see below.

```scala mdoc
val c = Counter(-5)

val c1 = Counter(10).copy(-5)

```

To fix this, we need to tweaks our smart constructor. We will need to define our own `apply` and `copy` to supress the synthetic ones.

```scala mdoc:reset
case class Counter private (value: Int) {
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
val c = Counter(-5)

val c1 = Counter(10).copy(-5)
``` 

## Using Sealed abstract case classes
```scala mdoc:reset
sealed abstract case class Counter private (value: Int) 
```

By using 
```scala mdoc

```
[refined]: https://github.com/fthomas/refined