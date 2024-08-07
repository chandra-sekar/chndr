---
title: Analysis Paralysis
date: 2019-02-11
layout: layouts/post.njk
description: Some ramblings on how I have been procrastinating on getting this website up
---

It's been nearly two years since I got this domain with the intention of setting up a personal website/blog. I have since then been thinking of different ways to set it up. This is just a long winding post that describes what I have tried so far. I am writing this mostly so that I have some content to work with, instead of just Lorem Ipsum.

I knew I pretty much wanted to build a website that followed <a href="https://indiewebify.me/" target="_blank">IndieWeb principles</a>. I found the movement intriguing. Being able to write on my own domain and syndicate this content to third party silos like Twitter and Medium was the main draw. I loved what Jeremy Keith was doing at <a href="https://adactio.com" target="_blank">adactio.com</a>

This lead to a period where I started considering the options I had to build this out. I am a developer. So, on some level, I wanted this to be a place where I experiment with new technology. I figured I wanted to use React, but making it a single page app seemed to be a bad fit, and overkill for a blog. I started considering various static site generators (<a href="https://jekyllrb.com/" target="_blank">Jekyll</a>, <a href="https://gohugo.io/" target="_blank">Hugo</a>, <a href="https://www.gatsbyjs.org/" target="_blank">Gatsby</a> et al) but figured I did not want a build step or make a commit everytime I wanted to update my blog. I wanted a posting interface that I could use when I was on the move.

It was around this time <a href="https://nextjs.org/" target="_blank">Next.js</a> launched. It seemed perfect for the job. It offered an easy way to build server rendered react pages. The pages loaded fast. And it did not even require Javascript to render itself on the browser. I was excited, and created a basic list view of blog posts.

But I still needed a writing interface, and I did not fancy having to open my text editor whenever I wanted to post something. So I started learning Node.js and built a rudimentary posting interface, where the entries would be stored inside MongoDB. I was new to server programming and got stuck at a point. Between other work, this lead to a gap of a few months, and when I came back to the project, the rudimentary blog I had built was throwing errors. The Next.js router API had changed, and they had also deprecated the CSS-in-JS solution they had launched with. I eventually fixed it. But I figured it was too early to adopt the framework (it was still in beta). And I felt I was not prepared to handle any issues that may come up later on, because I did not entirely understand how it worked.

Then I went the opposite direction, and started seeing if I could use a battle tested CMS. My first obvious choice was Wordpress. It had a neat IndieWeb plugin pack that set up IndieAuth, Micropub endpoints, and Post kinds. It worked. I was able to authenticate using IndieAuth and post to Twitter from my domain. I briefly used brid.gy for Webmentions and quill.p3k.io to post content and quite liked the setup. But the website was still only a tech demo at this point, and I had to build the actual theme before I could call it my home and invited people over. But working with PHP templates made me feel like I was spending time on something tangential to what I wanted to be learning. The page load speed was also not as fast as I wanted it to be. I was also concerned about keeping wordpress updated and breaking these plugins. I felt it was a good option, but I wanted to know if I could do better.

My next option was to use Wordpress as a headless CMS and combine it with Next.js (which I was ready to try again). I had a DigitalOcean box set up at this point. I had to figure out how to run both WP REST API and Next.js on the same box. I then stumbled upon the headless <a href="https://github.com/postlight/headless-wp-starter/" target="_blank">WP+React starter kit</a> which looked like exactly what I wanted. I got it working eventually, but it seemed slow (could be because I was running it on a $5 box!). But my bigger concern was the fact that it was opaque, with a lot of moving parts, that could break at any point. I figured I'd just leave it at this point and build something from scratch myself -\_-

In the board games community, there is a common term 'Analysis Paralysis' (or just 'AP') to describe when a player is stuck with too many options on their turn. It occurs when there are many interlocking possibilities in the game, and analysing each one of them takes up time. Turns take forever because the player is unable to arrive at a decision. This is exactly how I felt at this point, and it was mostly my doing.

Anyway, I saw Kylie's post last night and got this page up in a fit of excitement. I created a repo on Github, used their built-in editor to add some text, and pointed it to my domain using Github sites. And that's all it took to get something up.

Now, I hope to build on top of this, and not overthink again!
