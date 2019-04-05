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

Let's say we want to encode a `TimeFrame` which contains a `StartDate` and an `EndDate`.

We can use a case class to encode it.

```scala mdoc
import java.time.LocalDate

case class TimeFrame(startDate: LocalDate, endDate: LocalDate)
```

Now imagine that we want to impose a restriction on the allowed values. Let's say the `EndDate` should always be greater than `StartDate`.
There are a couple of ways we can achieve this which I will explore below.

## Using preconditions

Scala provides a set of preconditions that we use to validate our data, being one of them `require`.
We can use it in the following way.

```scala mdoc:reset
import java.time.LocalDate

case class TimeFrame(startDate: LocalDate, endDate: LocalDate) {
    require(endDate.isAfter(startDate))
}
```

This will make sure that we can't create an instance of `TimeFrame` that doesn't respect the restriction, as we can see bellow.

```scala mdoc:crash

TimeFrame(LocalDate.now, LocalDate.now)

```
As the values provided doesn't respect the pre conditiion, `require` will throw an exception.
While this solves our problem, it's not a good solution. 

The main issue is that it will fail at runtime. Also the user of this class, doesn't know that creating an instance can fail. This can lead to unexpected errors while running the application.

In this case we are not really using the compiler in our favour.
Let's see if we can leverage the scala type system and lift this restriction into the type level.

## Making the constructor private

Ideally, we would like to have a single mechanism to allow the creation of valid instances and make the user aware that it must deal with the possibility of failure.

To achieve this, we can mark the constructor of the case class private and provide a custom constructor that will do the required validation. This is usually called a smart constructor.
Let's see how it looks like.

```scala mdoc:reset

import java.time.LocalDate

val today = LocalDate.now
val yesterday = today.minusDays(1)

case class TimeFrame private (startDate: LocalDate, endDate: LocalDate)

object TimeFrame {
    def smartConstructor(startDate: LocalDate, endDate: LocalDate): Either[String, TimeFrame] = 
    if (endDate.isAfter(startDate)) 
        Right(TimeFrame(startDate, endDate)) 
    else 
        Left(s"Error: endDate [$endDate] must be after startDate [$startDate]")
}
```

```scala mdoc:fail
new TimeFrame(today, yesterday) {}
```
As you can see, we can no longer use the default constructor. 
Now that we have define our own constructor, we can create only valid instances.
```scala mdoc
val fail = TimeFrame.smartConstructor(startDate = today, endDate = yesterday)
```
The custom constructor now returns a `Either[String, TimeFrame]`. This informs our user that they must deal with a possible failure.
We are no longer rely on runtime validation but are instead using the compiler to inforce this using the type system.

From the example above, when we try to construct an invalid instance we will get a `Left` indicating that something has failed. The user has to explicitly handle this.

Given that we are using a case class, the compiler will generate some synthetic methods for us. Generally, these are quite handy but for our case they are causing some harm.

`apply` or `copy` are two of the synthetic methods generated. And they can be used to bypass our smart constructor as we can see below.

```scala mdoc
val t = TimeFrame(startDate = today, endDate = yesterday)

val t1 = TimeFrame(startDate = today, endDate = today.plusDays(1))

val t2 = t1.copy(endDate = yesterday)
```

To fix this, we need to tweaks our smart constructor. We will need to define our own `apply` and `copy` to supress the synthetic ones.

```scala mdoc:reset

import java.time.LocalDate

val today = LocalDate.now
val yesterday = today.minusDays(1)

case class TimeFrame private (startDate: LocalDate, endDate: LocalDate) {
    def copy(startDate: LocalDate = startDate, endDate: LocalDate = endDate): TimeFrame = TimeFrame(starDate, endDate)
}

object TimeFrame {
    def apply(startDate: LocalDate, endDate: LocalDate): TimeFrame = new TimeFrame(startDate, endDate)
}
```

```scala mdoc:fail
TimeFrame(today, today.plusDays(1)).copy(endDate = yesterday)
```


## Using Sealed abstract case classes
```scala mdoc:reset

import java.time.LocalDate

sealed abstract case class TimeFrame(startDate: LocalDate, endDate: LocalDate)
```