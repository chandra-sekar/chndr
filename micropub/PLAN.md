# Micropub Server Plan

A Clojure (JVM) micropub server deployed on Application Garden that accepts posts from micropub clients and commits them directly to the GitHub repo via the GitHub Contents API. Netlify detects the push and rebuilds the static site.

## Architecture

```
Micropub client
    │
    │ POST /micropub (Bearer token)
    ▼
Application Garden (chndr-micropub.apps.garden)
    │ JVM Clojure + http-kit
    │
    │ Token validation
    │ POST https://tokens.indieauth.com/token
    │
    │ File creation
    │ PUT https://api.github.com/repos/chandra-sekar/chndr/contents/...
    ▼
GitHub (chandra-sekar/chndr, branch: master)
    │
    │ push triggers build
    ▼
Netlify → chndr.cc
```

## Application Garden

- **Runtime**: JVM Clojure via `clojure -X:nextjournal/garden`
- **Port**: 7777 (platform requirement)
- **Health check**: `HEAD /` must return 200
- **Deploy strategy**: `:zero-downtime` (default) — server is stateless, no persistent storage needed
- **Secrets**: `GITHUB_TOKEN` (GitHub PAT with repo write access)

## Authentication

Uses IndieAuth.com as the hosted authorization and token endpoint — no custom token server required.

Three `<link>` tags in `_includes/layouts/base.njk`:

```html
<link rel="micropub" href="https://chndr-micropub.apps.garden/micropub">
<link rel="authorization_endpoint" href="https://indieauth.com/auth">
<link rel="token_endpoint" href="https://tokens.indieauth.com/token">
```

Token validation on each request: POST the Bearer token to `https://tokens.indieauth.com/token` and verify the returned `me` URL matches `https://chndr.cc/`.

## Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| `HEAD /` | — | Health check (returns 200) |
| `GET /` | — | Returns 200 (satisfies health check for GET too) |
| `GET /micropub?q=config` | — | Config query, returns `{}` |
| `POST /micropub` | — | Create note or article |

Accepted content types for POST:
- `application/x-www-form-urlencoded`
- `application/json`

## Post Types

**Note** (no `name` property):
- Path: `notes/<unix-timestamp>.md`
- URL: `https://chndr.cc/notes/<unix-timestamp>/`
- Frontmatter: `date` only

```markdown
---
date: 2026-05-02T10:30:00Z
---
Content here.
```

**Article** (has `name` property):
- Path: `posts/<slug>.md` (slug derived from `name`)
- URL: `https://chndr.cc/posts/<slug>/`
- Frontmatter: `title`, `date`, `layout`

```markdown
---
title: My Article Title
date: 2026-05-02T10:30:00Z
layout: layouts/post.njk
---
Content here.
```

## GitHub Contents API

Single `PUT` per post — no cloning, no git binary, fully stateless:

```
PUT https://api.github.com/repos/chandra-sekar/chndr/contents/<path>
Authorization: Bearer <GITHUB_TOKEN>
Content-Type: application/json

{
  "message": "Add note via micropub",
  "content": "<base64-encoded file content>",
  "branch": "master"
}
```

On success, GitHub returns 201 with the commit SHA. Micropub server returns:

```
HTTP/1.1 201 Created
Location: https://chndr.cc/notes/<timestamp>/
```

## File Structure

```
micropub/
  PLAN.md
  TODO.md
  garden.edn          # {:project "chndr-micropub"}
  deps.edn            # JVM Clojure deps + :nextjournal/garden alias
  src/
    micropub/
      core.clj        # Entry point, server start
      handler.clj     # Ring handler, routing
      auth.clj        # IndieAuth token validation
      posts.clj       # Note/article creation, GitHub API call
```

## Note on Babashka

Application Garden hardcodes `clojure -X:nextjournal/garden` as the runtime — Babashka is not a supported runtime. JVM Clojure is used instead. The code is identical in syntax to what you'd write in Babashka.
