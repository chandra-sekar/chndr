const path = require("path");
const { absoluteUrl } = require("@11ty/eleventy-plugin-rss");
const templateContentToFeedHtml = require("../filters/template-content-to-feed-html.js");

module.exports = async (collection, metadata, n = 10) => {
  const items = collection.slice(0, n);

  const feed = {
    version: "https://jsonfeed.org/version/1.1",
    title: "Chandrasekar - Front-end Developer",
    description: "The personal website of Chandrasekar",
    home_page_url: "https://chndr.me",
    feed_url: "https://chndr.me/feed/feed-notes.json",
    language: "en-GB",
    items: [],
    authors: [
      {
        name: "Chandrasekar",
        url: "https://chndr.me",
        avatar: "https://chndr.me/img/profile-pic.png",
      },
    ],
  };

  for (const item of items) {
    const feedItem = {
      id: absoluteUrl(item.url, metadata.url),
      url: absoluteUrl(item.url, metadata.url),
      date_published: item.date,
      content_html: await templateContentToFeedHtml(item, metadata),
    };
    if (item.data["bookmark-of"]) {
      feedItem.external_url = item.data["bookmark-of"];
    }
    feed.items.push(feedItem);
  }

  return JSON.stringify(feed, null, 2);
};
