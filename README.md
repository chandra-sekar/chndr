# chndr.cc

Chandrasekar's personal website built with [Eleventy](https://www.11ty.dev/).

## Features

- **Journal**: Long-form blog posts with titles
- **Notes**: Short-form content (tweet-like) with timestamps, image support, and links
- **Bookshelf**: Curated collection of favorite content
- **IndieWeb**: Microformats, Micropub, Webmention, POSSE to Mastodon via Bridgy
- **RSS/JSON Feeds**: Available for both posts and notes
- **Sass**: Compiled SCSS stylesheets
- **Netlify CMS**: Admin interface for content management

## Prerequisites

- Node.js (v18 or higher recommended - see `.nvmrc`)
- npm

## Getting Started

### Installation

1. Clone the repository:
```bash
git clone https://github.com/chandra-sekar/chndr.git
cd chndr
```

2. Install dependencies:
```bash
npm install
```

### Development

Start the development server with live reload:

```bash
npm run dev
```

The site will be available at `http://localhost:8080`

### Building

Build the site for production:

```bash
npm run build
```

The output will be in the `_site` directory.

### Other Commands

- `npm start` - Alias for `npm run dev`
- `npm run clean` - Remove the `_site` directory

## Project Structure

```
.
├── _11ty/              # Eleventy utilities
│   └── getTagList.js   # Tag collection helper
├── _data/              # Global data files
│   ├── metadata.json   # Site metadata
│   └── webmentions.js  # Fetches webmentions from webmention.io at build time
├── _includes/          # Templates and partials
│   ├── layouts/        # Layout templates
│   │   ├── base.njk    # Base HTML layout
│   │   ├── home.njk    # Homepage layout
│   │   ├── note.njk    # Note layout
│   │   ├── page.njk    # Page layout
│   │   └── post.njk    # Blog post layout
│   ├── noteslist.njk   # Notes listing component
│   └── postslist.njk   # Posts listing component
├── admin/              # Netlify CMS configuration
│   └── config.yml      # CMS config
├── css/                # Stylesheets (SCSS)
│   ├── _base.scss
│   ├── _reset.scss
│   ├── _theme.scss
│   ├── _utilities.scss
│   ├── _variables.scss
│   └── style.scss      # Main stylesheet
├── feed/               # RSS/JSON feed templates
│   ├── feed.njk        # XML RSS feed
│   └── feed-notes.njk  # JSON feed for notes
├── img/                # Images
├── lib/                # Custom functions
│   ├── filters/        # Custom Eleventy filters
│   └── shortcodes/     # Custom Eleventy shortcodes
├── notes/              # Note entries (short-form)
│   ├── notes.json      # Directory data file
│   └── *.md            # Individual notes
├── posts/              # Blog posts (long-form)
│   ├── posts.json      # Directory data file
│   └── *.md            # Individual posts
├── about/              # About page
├── bookshelf/          # Bookshelf page
├── journal/            # Journal listing page
├── .eleventy.js        # Eleventy configuration
└── package.json        # Dependencies and scripts
```

## Content Management

### Creating a Blog Post

Create a new markdown file in the `posts/` directory:

```markdown
---
title: "Your Post Title"
date: 2025-11-08
layout: layouts/post.njk
tags:
  - posts
  - your-tag
---

Your post content here...
```

### Creating a Note

Create a new markdown file in the `notes/` directory:

```markdown
---
date: 2025-11-08T09:00:00.000+00:00
photo:
  - url: /img/your-image.jpg
    alt: "Image description"
---

Your short note content. Can include [links](https://example.com) and **formatting**.
```

### Using Netlify CMS

Access the admin interface at `/admin/` when running locally or on your deployed site. You'll need to authenticate with Netlify Identity.

## Configuration

### Site Metadata

Edit `_data/metadata.json` to update site information:

```json
{
  "title": "Your Site Title",
  "url": "https://yoursite.com",
  "description": "Site description"
}
```

### Eleventy Config

The main configuration is in `.eleventy.js`. Key features:

- **Filters**: Custom date formatting, array helpers
- **Collections**: Auto-generated from tags
- **Passthrough Copy**: Images, fonts, static assets
- **Markdown**: markdown-it with anchor links
- **SCSS**: Automatic compilation to CSS

## Deployment

The site is configured for deployment on Netlify:

1. Push changes to your GitHub repository
2. Netlify will automatically build and deploy
3. Build command: `npm run build`
4. Publish directory: `_site`

## Feeds

- **RSS Feed (Posts)**: `/feed/feed.xml`
- **JSON Feed (Notes)**: `/feed/feed-notes.json`

## IndieWeb Features

This site supports IndieWeb standards:

- **Microformats**: h-entry, h-card, h-feed, dt-published, u-syndication
- **Micropub**: Endpoint at `https://chndr-micropub.apps.garden/micropub`
- **IndieAuth**: Authorization via `https://indieauth.com/auth`, tokens via `https://tokens.indieauth.com/token`
- **Webmention**: Endpoint at `https://webmention.io/chndr.cc/webmention`
- **POSSE**: Notes are syndicated to Mastodon via Bridgy; backfeed (likes, replies, boosts) is displayed on each note
- **rel=me**: Links for identity verification

## Micropub Server

The site accepts posts from any [Micropub](https://micropub.spec.indieweb.org/) client (tested with [Quill](https://quill.p3k.io) and [micropublish](https://micropublish.net)).

### How it works

```
Micropub client
    │ POST /micropub (Bearer token)
    ▼
chndr-micropub.apps.garden   (Clojure/http-kit on Application Garden)
    │ Validates token via IndieAuth
    │ Commits file via GitHub Contents API
    │ (async) POST https://brid.gy/micropub → Mastodon post created
    │ (async) Writes syndication: <mastodon-url> back to note frontmatter
    ▼
GitHub (master branch)
    │ push triggers Netlify build
    ▼
chndr.cc  (note now has u-syndication link)
    │
    └── When someone interacts with the Mastodon post:
        Bridgy finds u-syndication link via POSSE-post-discovery
        → sends webmention to webmention.io
        → webmention.io webhook triggers Netlify rebuild
        → like/reply/boost appears on the note
```

### Post types

**Note** (no `name` property) — committed to `notes/<unix-timestamp>.md`:

```markdown
---
date: 2026-05-03T10:30:00Z
syndication: https://mastodon.social/@chander/123456  # added after Bridgy syndication
---
Content here.
```

**Article** (has `name` property) — committed to `posts/<slug>.md`:

```markdown
---
title: My Article Title
date: 2026-05-03T10:30:00Z
layout: layouts/post.njk
---
Content here.
```

### Source

The server lives in `micropub/` and is a standard Clojure deps project:

```
micropub/
  deps.edn            # http-kit, data.json, ring-core; :test alias
  garden.edn          # {:project "chndr-micropub"}
  src/micropub/
    core.clj          # starts http-kit on port 7777
    handler.clj       # Ring routing, request parsing
    auth.clj          # IndieAuth token validation
    posts.clj         # file building, GitHub Contents API, Bridgy syndication
  test/micropub/
    handler_test.clj  # HTTP behavior tests
    posts_test.clj    # post content and slug tests
```

### Deployment

The server runs on [Application Garden](https://application.garden) as `chndr-micropub`. Required secrets:

| Secret | Description |
|---|---|
| `GITHUB_TOKEN` | Fine-grained PAT with Contents write access to this repo |
| `BRIDGY_MASTODON_TOKEN` | Bearer token from brid.gy for Mastodon syndication |

```bash
cd micropub
garden deploy          # deploy latest commit on master
garden secrets list    # verify secrets are set
```

### Running tests

```bash
cd micropub
clojure -X:test
```

70 behavioral tests covering HTTP routing, authentication, form and JSON post creation, media uploads, bookmarks, Mastodon syndication, and syndication URL writeback. External dependencies (IndieAuth, GitHub API, Bridgy) are stubbed.

## Webmention Setup

Webmentions are received via [webmention.io](https://webmention.io) and fetched at build time from `_data/webmentions.js`. Likes, boosts, and replies from Mastodon appear on each note and post after a Netlify rebuild.

### Required Netlify environment variable

| Variable | Description |
|---|---|
| `WEBMENTION_IO_TOKEN` | API token from webmention.io (sign in with IndieAuth at chndr.cc) |

### Auto-rebuild on new webmention

In webmention.io settings, set the notification webhook to your Netlify build hook URL. This triggers a rebuild automatically whenever a new webmention arrives, so interactions appear within a few minutes.

### Bridgy backfeed

Bridgy discovers which blog posts correspond to which Mastodon posts via the `u-syndication` links on notes (added after syndication). The notes page (`/notes/`) exposes an `h-feed` with these links; the homepage has `<link rel="feed" href="/notes/">` so Bridgy can be pointed at `https://chndr.cc/` directly.

A dedicated machine-readable h-feed at `/notes/feed/` lists all notes with their syndication URLs, so Bridgy can match interactions on older posts that have scrolled off the paginated notes page.

## Dependencies

### Core
- `@11ty/eleventy` - Static site generator
- `luxon` - Date/time handling
- `sass` - CSS preprocessing

### Plugins
- `@11ty/eleventy-plugin-rss` - RSS feed generation
- `@11ty/eleventy-plugin-syntaxhighlight` - Code syntax highlighting

### Markdown
- `markdown-it` - Markdown parser
- `markdown-it-anchor` - Auto-generate heading anchors

## Browser Support

Modern browsers (last 2 versions):
- Chrome
- Firefox
- Safari
- Edge

## License

ISC

## Author

Chandrasekar - [https://chndr.cc](https://chndr.cc)

## Links

- [Repository](https://github.com/chandra-sekar/chndr)
- [Issues](https://github.com/chandra-sekar/chndr/issues)
- [Eleventy Documentation](https://www.11ty.dev/docs/)
