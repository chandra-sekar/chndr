# Micropub Server Tests

Behavioral tests that verify HTTP responses and post content — not implementation
internals. Tests mock external dependencies (IndieAuth, GitHub API) at the function
boundary so the implementation can be freely refactored.

## Test runner

```
clojure -X:test
```

## Dependencies

- `ring/ring-mock` — constructs Ring requests without a running server
- `org.clojure/test.check` — not needed for now

## Test files

### `test/micropub/handler_test.clj`

Tests the full HTTP request/response cycle via the Ring handler.
External calls (`auth/validate-token`, `posts/create-post`) are stubbed with `with-redefs`.

#### Health check

- [x] `HEAD /` returns 200
- [x] `GET /` returns 200
- [x] `GET /unknown-path` returns 404

#### Micropub config

- [x] `GET /micropub?q=config` returns 200
- [x] `GET /micropub?q=config` returns `Content-Type: application/json`
- [x] `GET /micropub?q=config` returns body `{}`
- [x] `GET /micropub?q=config&extra=foo` also returns config (extra query params don't break it)

#### Authentication

- [x] `POST /micropub` with no `Authorization` header returns 401
- [x] `POST /micropub` with an invalid token returns 403

#### Form-encoded post creation

- [x] Valid token + `content=...` returns 201
- [x] Note (no `name`) returns `Location: https://chndr.cc/notes/<timestamp>/`
- [x] Article (`name` + `content`) returns `Location: https://chndr.cc/posts/<slug>/`
- [x] Missing `content` returns 400

#### JSON post creation

- [x] JSON note (`type: h-entry`, no `name` property) returns 201
- [x] JSON article (`name` + `content` under `properties`) returns 201
- [x] JSON with `content` as `{"html": "..."}` map returns 201 (not 400)

#### GitHub failure

- [x] When `posts/create-post` returns `:error`, server returns 500

---

### `test/micropub/posts_test.clj`

Tests the content that would be committed to GitHub — file paths, frontmatter, and
body — by calling `posts/build-note` and `posts/build-article` directly (or via a
refactored pure helper). Does **not** make real GitHub API calls.

#### Note structure

- [x] File path matches `notes/<unix-timestamp>.md`
- [x] Frontmatter contains `date:` and nothing else
- [x] Body follows the frontmatter

#### Article structure

- [x] File path matches `posts/<slug>.md`
- [x] Frontmatter contains `title:`, `date:`, and `layout:`
- [x] Slug lowercases the title
- [x] Slug replaces spaces with hyphens
- [x] Slug strips special characters
- [x] Body follows the frontmatter
