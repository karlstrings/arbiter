# Arbiter
This is *not* a serious product. This is my opinionated dependency injection engine I use for personal projects. I found 
that Spring was too heavy and Guice wasn't really my style. So I made this. I licensed it under as CC BY 
(see: https://creativecommons.org/licenses/by/4.0/) so you can do pretty much whatever you want with it.

I have found Arbiter works really well in slapdash mobile apps where something like Dagger requires too much config 
juggling, games (hey, JNAing SDL2 does work...), and AWS Lambda functions. I have also written an old school MUD server 
with this thing.

## Support?
Probably not. I have a real job and this is only used for my personal projects. I will add features and fix bugs as they
are needed for my stuff. Maybe. 

You are on your own, so if you use this is 'serious' projects accept the fact that you may need to fork.

## Goals
* Idiot simple
* Single jar with no external dependencies
* No classloader shenanigans

## Non Goals
* JSR 330
* Support for ambiguous type differentiation (@Qualifier)
* Supporting scoped instantiation

## Features
* Injection of components
* Injection of configuration based on profiles
* Uses interfaces and constructors to keep things clean
