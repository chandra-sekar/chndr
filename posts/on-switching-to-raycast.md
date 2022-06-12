---
title: On switching to Raycast
date: 2021-06-19T04:49:49.462Z
layout: layouts/post.njk
---
I am a big fan of the command palette UI pattern. So much so that I am sure `cmd+k` in Slack, `cmd+p` in Sublime text, and `cmd+space` for Alfred are my three most used keyboard shortcuts on a daily basis. You can read more about this pattern in this nice blog post that describes it along with examples - [Command Palette Interfaces ](https://philipcdavis.com/writing/command-palette-interfaces)
---
My first introduction to a command palette was with [Quick Silver](https://qsapp.com) on Mac OSX Tiger. It was quickly replaced by [Alfred](https://www.alfredapp.com). The pattern became popular enough for Apple to change Spotlight's UI with Yosemite.

I have been using Alfred for more than five years and would have counted myself a loyalist if anyone had asked me. Alfred offers a lot of functionality as part of its paid power pack which I never ended up using. My usage was limited to using it as an app launcher and to execute some basic commands like force quitting apps. I had also set up some shortcuts to open some frequently visited web apps. It was one of my most used apps and I had no problems with Alfred whatsoever.

I don't know how I came across [Raycast](https://raycast.com). I remember looking at the website and thinking it looked cool. In particular, the calendar integration that allowed me to jump into a meeting without opening my calendar or mail caught my eye. But I was in no mood to switch from trusted Alfred where I had already spent some effort configuring shortcuts that I used daily.

Then everytime I had to join a meeting link or find a Google Doc, in the back of my mind, I knew there was a better way to do it. It kept surfacing in my mind a few times before I decided to give it a shot one afternoon. I installed Raycast and it took 15 minutes to move my old shortcuts from Alfred to Raycast manually. I changed my Alfred keybinding (`cmd+space`) to open Raycast. It has been a few weeks since then and I know I am not going back to Alfred. All it took was 15 minutes to switch from an app that was a daily habit for more than 5 years. 

Why was the switch easy? 
One big reason was because I did not have to change my daily habit of pressing `cmd+space`, typing an app's name, and pressing enter. Which is what I do whenever I want to launch an app. The second reason is because Raycast did a great job at surfacing all the things I could do with it and made it easy to connect my Calendar, Google Drive, and gave me a list of other features that I could enable and start using immediately. I was sold the minute I could search my Google drive without opening a browser and jump into an upcoming meeting without hunting for the meeting link.

The plot twist is, Raycast is potentially replacing a number of apps I use on a daily basis. Not just Alfred. It replaces the following utilities which I use:

* A clipboard manager (Replaces Jumpcut)
* A window manager (Replaces Rectangle)
* A plain text calculator (Kind of replaces Numi/Soulver. For example, lets you type '23 is what % of 524' to get the answer)
* A scratch pad for notes (Replaces Tyke)

I am sure they have more upcoming. I think what Raycast has done is look at the 'greatest hits' list of mac utilities and try to build it all within a single app. And it simply works. They also offer some deep integration with services like Github with nested commands and actions. They also seem to offer a developer API which I want to try using sometime. Earlier, someone had to be a tweaker and keep hunting for these utilities and try them out of their own interest. Now everything comes neatly packaged in a well integrated whole. In an ideal world, all the above utilities would be standard operating system features but our tech overlords nowadays are more interested in building more marketable features. Needless to say, I highly recommend Raycast.

Why is Alfred behind?
It is not like Alfred is not actively maintained. I used to receive regular updates. I think they are a small team and got caught up in their more advanced features and maintaining them. They were focussing on their existing user base and satisfying the needs of their power pack users. To be fair, those are the ones that pay for the software. But meanwhile, the world around them changed. The number of knowledge workers is only growing. You no longer have to be an 'enthusiast' to be able to appreciate these improvements that remove friction from your day to day work. Most people spend a lot of time inside web apps like their email, calendar, bug/task tracker or version control app. First class support for deep integration with these web services is the killer feature of Raycast. Alfred is from an era where files and folders on the computer were the way to manage your data. Raycast allows API endpoints to be on the same level as files and folders.

Some of the things I mentioned above is probably already available on Alfred using workflows and extensions on Alfred. But it requires work. Feature discovery is also poor in Alfred where you are met with a blank prompt to type something instead of surfacing all the different things you could do. It took me three years after using Alfred to set up my first shortcut. It requires you to go into a preference pane to configure everything and it is not staight forward. It looks like Alfred has been caught sleeping at the wheel and I hope they wake up.

Maybe some of this is harsh because Alfred seems to be maintained by a few indie developers whereas Raycast is backed by venture money. I don't know what Raycast's business model is. They offer all this for free and it is installed at my operating system level with potentially access to all my data including web services. I don't feel very comfortable with this. Maybe the time saved using it is not worth it. Which is why I hope Alfred improves in these regards and all of us have an indie alternative. I know I will personally switch back if they simply bring better Google calendar and Google drive integration. Until then, I continue to enjoy using Raycast. Give it a shot if you find it interesting.