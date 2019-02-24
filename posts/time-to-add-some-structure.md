---
title: Time to add some structure
date: 2019-02-13
layout: layouts/post.njk
---

I have reached a point where writing new content is getting unwieldly. I have to keep adding markup and dates for all the content manually, and the page is getting too lengthy. Time to give the computer some work :)

A good static site generator should take care of a lot of this stuff. Now, I have been in this situation before, and I don't want to get stuck in this loop again.

I have decided to use <a href="https://www.11ty.io/" target="_blank">Eleventy</a> for the build and I am going to commit to it. I picked it for the following reasons:

<ul>
	<li>It allows me to pick a templating language of my choice, including just plain HTML.</li>
	<li>It does not enforce a strict directory structure.</li>
	<li>It is written in Node, and I can use Javascript. I don't have to introduce another language into the mix.</li>
	<li>It has flexible options for fetching data.</li>
	<li>It has a <a href="https://mxb.at/blog/syndicating-content-to-twitter-with-netlify-functions/" target="_blank">proven</a> <a href="https://mxb.at/blog/using-webmentions-on-static-sites/" target="_blank">record</a> of being used to build a site with <a href="https://indiewebify.me/" target="_blank">IndieWeb</a> capabilities.</li>
</ul>

I read through Eleventy's <a href="https://www.11ty.io/docs/" target="_blank">documentation</a> and everything seemed to make sense. I think reading Max Böck's post (linked above) totally convinced me about this choice. <a href="https://mxb.at/" target="_blank">His site</a> looks beautiful and if my page can look half as good, I will be happy. 

So Eleventy it is.