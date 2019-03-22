## This is the source code for my blog.

The blog is a static site generated with [Hugo][hugo] and hosted in [GitHub Pages][ghpages].
We are also using [Mdoc][mdoc] to compile the scala code snippets, so we always generate correct code.
To help us with the site generation we use [sbt-site][sbt-site] and for publishing to ghpages we use [sbt-ghpages][sbt-ghpages]

### Blog post
Blog posts should be created in the [`posts`](posts) directory. See existing posts for examples.

### Validating scala snippets
After the post is created, we need to make sure that the code is actually correct. Execute the following to compile the source code
```bash
sbt mdoc
```

### Preview the blog 
To generate and preview the blog.
```bash
sbt previewSite
```
### Publishing to [GitHub Pages][ghpages]
To publish the blog, execute the folowing.
```bash
sbt ghpagesPushSite
```

### Live reloading

This setup was heavily inspired by [Viktor Lövgren's blog][vlovgr]

[mdoc]: https://scalameta.org/mdoc/
[hugo]: https://gohugo.io/
[ghpages]: https://pages.github.com/
[sbt-ghpages]: https://github.com/sbt/sbt-ghpages
[sbt-site]: https://github.com/sbt/sbt-site
[vlovgr]: https://github.com/vlovgr/blog