const path = require("path");
const { absoluteUrl } = require("@11ty/eleventy-plugin-rss");
const templateContentToFeedHtml = require("../filters/template-content-to-feed-html.js");

module.exports = async (collection, metadata, n = 10) => {
  const items = collection.slice(0, n);

  const feed = {
    version: "https://jsonfeed.org/version/1.1",
    title: "Chandrasekar - Front-end Developer",
    description: "The personal website of Chandrasekar",
    home_page_url: "https://chndr.cc",
    feed_url: "https://chndr.cc/feed/feed-notes.json",
    language: "en-GB",
    items: [],
    authors: [
      {
        name: "Chandrasekar",
        url: "https://chndr.cc",
        avatar: "https://chndr.cc/img/profile-pic.png",
      },
    ],
  };

  for (const item of items) {
    feed.items.push({
      id: absoluteUrl(item.url, metadata.url),
      url: absoluteUrl(item.url, metadata.url),
      date_published: item.date,
      content_html: await templateContentToFeedHtml(item, metadata),
    });
  }

  const json = JSON.stringify(feed, null, 2);
  console.log(json);
  return json;
};
