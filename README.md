# WL4Edit

![preview](/res/preview.png?raw=true "preview")

## What?

WL4Edit is meant to eventually be a level editor for Wario Land 4; probably not as fully-featured as Lunar Magic for Super Mario World, but similar in core functionality. WL4Edit is currently in very early development and can't modify anything yet, but it can perform a lot of difficult decoding so far:

- Can read indexed 8x8 tile graphics directly from the ROM
- Can read palette data from the ROM and apply it to 8x8 tiles
- Can read map16 data which composes tiles and palettes into 16x16 pixel blocks
- Can decompress and render most levels using the above data

## How?

I am using NO$GBA Debugger 2.6a to step through execution, place breakpoints, and find data; I am using gbatek as a resource to describe where to start looking for data so I can work backwards and find where it's loaded from.

For example, set a breakpoint on writes to layer 1 -> an 8x8 tile gets loaded from map16 data. You've found the map16 data! But how did it know which map16 tile to load? Work backwards a bit -> find a location in working ram containing the tile -> breakpoint on that location -> you find the decompression algorithm -> You've found map data! Step until you go back a few stack frames (bx Rx) to find level loading function -> break on, and step through level loading function -> find uses of DMA channel 3 with palette memory as the destination -> You've found palette data! Etc, etc, etc

## Why?

Wario Land 4 is certainly not the most robust of platforming games, featuring fairly limited terrain interaction and a paltry 18 levels compared to the bigger platforming giants, such as the Super Mario Bros games, Super Mario World, etc. However, Wario Land 4 was one of my favorite childhood games on the GBA, and thus holds a special place in my heart. Also, ROM hacking is what introduced me to computer science, which I now have a degree in; going back to ROM hacking is a fun throwback for me, and a giving me a bit of programming, and low-level debugging experience to boot.

## Fair use?

WL4Edit does not contain any copyrighted assets. All graphics displayed are instead interpreted as data from a ROM file, which is itself the copyrighted data, and is not distributed here. DMCA section 1201(a)(1) makes exemptions for copyrighted data described as "Computer programs and video games distributed in formats that have become obsolete and which require the original media or hardware as a condition of access." So just make sure that the ROM you use (USA/European version) was ripped directly from your own, store-bought cartridge! You wouldn't illegally download it from somewhere... right?

## Contact?

Questions? Comments? Suggestions? Want to help? I'm all ears! Send me a pull request, or contact me: andrew DOT m DOT wilder AT gmail DOT com
