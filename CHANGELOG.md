# Change log for Android @Anytype app.

## Version 0.22.6

### New features & enhancements 🚀

* App | New Home screen or Widgets (...)
* App | New Space screen (...)
* App | Introduce library of Types and Relations (#2856, ...)
* App | New "About app" screen (...)
* App | File storage limits screen (...)
* App | New object type - Collection (...)
* Sets or Collections | Unblock Relations in Object menu (...)

### Fixes & tech 🚒

* App | Removed invite code screen (#40)
* Editor | Do not allow changing icon when object is locked (...)
* Editor | Do not allow formatting text when object is locked (...)
* Editor | Allow editing bookmark block with invalid link (...)
* App | Allow debugging your workspace or a specific object (...)

### Design & UX 🔳

* Editor | Design review fixes (...)
* Sets or Collections | Design review fixes (...)
* Settings | Profile screen redesign (...)

## Version 0.14.0

### New features & enhancements 🚀

* Editor | Introduced inline-set block (#2864)
* Sets | Optimised performance by integrating atomic changes for filters, sorts, relations and view updates in data view (#2866)

### Fixes & tech 🚒

* Editor | Line breaks should not be ignored inside text blocks (#2823, #2783)
* Editor | Allowed webp format for image blocks (#2861)
* Editor | Do not crash when dismissing move-to dialog with open keyboard (#2899)
* Editor | Style panels should not leak click events to block action panel (#2894)
* Editor | Should not allow editing read-only-value relations via featured-relations block (#2835, #2890)
* Editor | Object should be closed when moved to bin (#2844, #2870)
* Editor | Block-action panel should be hidden after move-to action (#2903)
* Sets | Cover and icon are not always displayed in sets (#2848, #2914)
* Sets | Data view 'source' renamed to data view 'query' (#2874)
* Relations | Relation status might disappear from list of relations when there is no value set (#2897)
* Relations | Should filter out system object types when searching for values for Relations with object format (#2891)
* Analytics | Fixed 'createObject' event (#2926)
* Search | Fixed last item's padding (#2808)
* App | Renamed `marketplace` Types and Relations into `library` types and relations (#2849)

### Design & UX 🔳

* Editor | Unified haptic feedback when selecting blocks (#2824)
* Relations | Introduced more user-friendly UX for setting status value (#2814)
* Sets | Showing lock-icon in grid-view column for read-only relations (#2829)
* Sets | View settings might have incorrect position on the screen when coming back to an open set (#2812)
* Search | Added section `Recently opened` recent-object search results in global search (#2822)

## Version 0.13.0

### New features & enhancements 🚀

* Decluttering | Introduce marketplace relations vs. user library relations (#2722, #2750, #2764)
* Decluttering | Introduce marketplace types vs. user library types (#2721, #2748, #2771)
* Editor | Simple tables 2.0: cell value can now be edited directly; @-mentions can be used in cells; introduced cell menu allowing user to clear content and style; introduced select-mode for cells;  introduced row menu allowing user to move, highlight, delete or duplicate rows; introduced column menu allowing user to move, highlight, delete or duplicate columns; drag-and-drop is enabled for simple table block (#2681, #2696, #2700, #2702, #2710, #2712, #2716, #2739, #2736)
* Editor | Style your link-to-object block as a card or as a text link (#2630, #2643) [ENABLED IN THIS RELEASE]
* Editor | Enable checking / unchecking link-to-object blocks referencing task objects (#2708)
* Editor | Showing object pre-loading state (#2791) 
* Sets | Enhancement | Support sets of objects aggregated by relations (#2727, #2731, #2740)
* Sets | Allow possibility to change data view source after its initial setup (#2692, #2698)
* Sets | Allow creating a set of sets (#2698) [DISABLED IN THIS RELEASE]
* Sets | Enabled 'duplicate' action for currently open set (#2697)

### Fixes & tech 🚒

* Editor | Enable ENTER key for external keyboards (#2747) 
* Editor | Mention widget are not always shown reliantly when typing '/' character (#2703) 
* Editor | Enabled search-on-page functionality for simple tables (#2664, #2669, #2670)
* Editor | Horizontal type panel should not be visible when type is selected from type picker (#2807)
* App | Fix | Rapid double clicks sometimes would case redundant navigation or even application crash (#2762) 
* Objects | Adding link to currently open object from object or set via object's three-dots menu (#2668)
* Sets | Covers of objects with images picked from Unsplash wouldn't be displayed correctly in gallery view (#2713)
* Search | Last search result might not be always visible (#2808)
* Account | Delete-account screen should be removed from navigation stack when user cancels deletion (#2815)

### Design & UX 🔳

* Editor | Make bold font more bold (#2755) 
* Editor | Remove useless forward/backward buttons from video block (#2769)
* Editor | Fixed margins for video block in uploading state (#2724)
* Editor | Fixed incorrect expand-and-collapse arrow direction in horizontal type panel (#2810)
* App | Disable login button on an empty field (#2777)

## Version 0.12.0

### New features & enhancements 🚀

* Editor | Introducing nested styling: children of callout or quote blocks are now able to inherit style from parent. Also, backgrounds are now nested: children inherit background color from its parent blocks (#2409)
* Editor | Style your link-to-object block as a card or as a text link (#2630, #2643) [DISABLED IN THIS RELEASE]
* Objects | Show pop-up notifications when creating links between two objects (2648)

### Fixes & tech 🚒

* Editor | The text typed to filter the slash menu options should be removed when an option is selected in numbered list items (#2620)
* Editor | Do not allow to create a link-to-object referencing currently open object (#2661)
* Sets | Date is displayed incorrectly in list view and in gallery view (#2639, #2657)

### Design & UX 🔳

* App | Setting cursor into search bar across different app menus (#2624)
* Sets | Empty state for sets without selected source (#2688)

## Version 0.11.0

### New features & enhancements 🚀
* Editor | Enhanced link markup menu by allowing to add url from clipboard. (#2581)
* Editor | Allow creating sets from objects in draft state and setting type for newly created set (#2580)
* Editor | Loading state for bookmark block (#2559)
* Editor | Improved mechanism for opening files with default apps from your device (#2568)

### Fixes & tech 🚒
* Editor & Set | Fix navigation crashes (#2593)
* Editor | Fixed inconsistent state of link markups when switching between read and edit modes (#2588)
* Editor | Allow navigating to the @-mentioned bookmark object (#2573)
* Editor | Clicking on empty space above the title block should not trigger multi-select mode (#2585)
* Editor | Incorrect bookmark block image (#2569)
* Editor & Sets | Text changes occurring in the title block might not be saved correctly due to data races (#2567)
* Sets | Application crash when entering big number (#2621)

### Design & UX 🔳
* Set | Group button removed (#2595)
* Editor | Show default size for simple tables in the slash menu (#2591)
* Editor | Redesigned error state for bookmark block (#2562)
* Dashboard | New icon for bookmark object (#2558)

## Version 0.10.0

### New features & enhancements 🚀

* Objects | Introduced reloadable bookmark object (#2504)
* Editor | Introduced underline markup (#2508)
* Sets | Pre-populate records with data taken from filters when creating new record (#2517)

### Fixes & tech 🚒

* Dashboard | Should filter profile objects in the "recent" tab (#2510)
* Sets | Do not allow multiple sorts of the same relation (#2497)

### Design & UX 🔳

* Editor | Loading state for bookmark block (#2559)
* Editor | Show Loading state when opening object (#2473)
* Editor | Draw diagonal line for circle designing default background or text color color in color (#2502)
* Editor | Improve keyboard experience by taking advantage of the new WindowInsets API (#2524)
* Editor | Make "Create new object" the last option of the mention menu (#2519)
* Editor | Introduced archived state for mentions (#2518)
* Dashboard | Tab headers should not jump when switching between tabs (#2509)
* Dashboard | Hide bin object menu when scrolling (#2505)
* Sets | When url is too long, create button overlaps with url when trying to create a bookmark from set (#2479)
* Sets | Introduced relative values for date filters (#2452)
* Settings | Added "Show and copy phrase" button to "Backup your recovery phrase" screen (#2487)

## Version 0.9.0

### New features & enhancements 🚀

* Editor | Introduced simple tables (#2427)
* Editor | Introduced callout block (#2432)
* Editor | Introduced nested styles: children blocks inheriting styles from parent blocks (#2459)
* Editor | Enabled text and background color pickers for title block (#2392)
* Sets | Introduced bookmark set for creating and browsing bookmark objects (#2428)

### Fixes & tech 🚒

* Objects | Hide unavailable actions in object menu (#2393)
* Objects | Object with cover and todo layout might have invalid layout (#2403)
* Objects | When moving an object to bin the confirmation message shows "Archived" instead of "Moved to bin" (#2461)
* Objects | Close current object after moving it to bin (#2451)
* Editor | Do not show unavailable actions for title block (#2395)
* Editor | Add possibility to upload a file into a media block in error state (#2351)
* Editor | Should not trigger keyboard opening when tapping on object's top toolbar (#2399)
* Editor | Copy/paste is not working for table of contents (#2441)
* Editor | Can't create an object using the @ mention option due to an internal error (#2455)
* Editor | Creating objects in objects via the mention (@) option might crash app (#2456)
* Search | Keyboard should be dismissed when navigating from search results to sets of objects (#2416)
* Sets | Grid View headers should show or hide object icon in row headers according to the view settings (#2408)
* Sets | Updated screen text for sorts for checkbox relations (#2424)
* App | Fixed application startup error when resuming Anytype after app is killed by the Android OS to release resources (#2447) 
* App | Resolved flaking remaining days count issue after account deletion (#2414)

### Design & UX 🔳

* Editor | Use snackbars instead of toasts to prevent overlapping undo/redo controls (#2412)
* Editor | File block in uploading state has incorrect border color in dark mode (#2406)
* Editor | Object icon is displayed in object's top toolbar when header is collapsed (#2397)
* Objects | Fix wrong text color in feature relations (#2407)
* Editor | Dark mode fixes (#2450)
* Settings | Some items have incorrect bottom offset on wallpaper-picker screen (#2398)
* Settings | Using skeleton view instead of blurring recovery phrase (#2394)
* App | Updated color for warning buttons (#2418)

## Version 0.8.0

### New features & enhancements 🚀

* App | Danger zone: allow account deletion (#2333)
* Editor | Improved markup shortcuts supported for all basic text blocks (#2275)
* Editor | Allowing to merge description with title in header (#2303)
* Editor | Support palette colors for background and text in title blocks (#2326)
* Editor | Fallback to basic paragraph when pressing backspace in empty bulleted, checkbox, toggle or numbered blocks (#2274)
* Sets | Shortcut way for editing your view from settings panel (#2285)
* Relations | Disable / enable relations editing based on object restrictions (locked state, read-only relations, etc.) (#2258)
* Objects | Show menu options based on object layout type and object restrictions (#2313)

### Fixes & tech 🚒

* Object | Filter out already existing relations when adding relations to object (#2309)
* Editor | Supporting tab indentation in code-snippet blocks (#2240)
* Editor | "Preview" action for link-to-object block should be among the first visible actions (#2321)
* Editor | Filter out current object when searching objects for move-to operation (#2304)
* Editor | Restore media block meta data when application is destroyed by the Android OS (#2294)
* Editor | Search results for move-to operation are now sorted by last modification date (#2269)
* Sets | Make long text ellipsized in headers of rows (#2276)
* Dashboard | Activated checkbox of task / todo objects is not displayed in the history (#2322)
* Relations | Filter out already existing tags when adding values to relation (#2262)
* Show action icon for relations URL, Email, Phone based on relation value: when value is present, action button is enabled, when not, it is disabled (#2290)
* Search | Display profile objects in search results (#2271)
* Auth | Fix | Disable that awful QR-code scanning sound (#2267)
* Auth | Trim recovery phrase when needed (#2266)
* Tech | Stop supporting Android 7 (#2268)

### Design & UX 🔳

* Editor | Better DND: when dropping the dragged block without changing its initial position, editor enters select mode (#2261)
* Editor | Better support for different appearance settings of link-to-object blocks (#2342))
* Editor | Less aggressive error state for media blocks (#2277)
* Dashboard | Buttons reordering (#2330)

## Version 0.7.0

### New features & enhancements 🚀

* Editor | New block: Table of contents (#2208)
* Editor | Duplicate your object via object menu (#2195)
* Editor | Select & copy blocks via block action menu (#2187)
* Editor | Show sync status description on long click (#2181)  
* Editor | Add possibility to upload an image by clicking on avatar in objects with profile layout (#2186)
* Objects & Sets | Introduced templates (#2212)
* Settings | Introduced theme (light/dark) selector (#2178)

### Fixes & tech 🚒

* Editor | Fixed inconsistent toggle states (#2164)
* Editor | Lifecycle-aware video block — stopped or paused when needed (#2173)
* Editor | Style toolbar would not always display correctly selected state (#2185)
* Editor | Numbered block won't change the text color of the number when the text color is updated (#2206)
* Editor | When object with TODO layout is locked, checkbox state should be in read mode (#2206)
* Editor | Display generic error message when failed to fetch images from Unsplash (#2205)
* Editor | Show avatar with initials when object with profile layout has no image (#2204)
* Editor | Link-to-object block should support indentation (#2201)  
* Editor | Link-to-object card should display content snippet when description is missing (#2193)
* Editor | When you select file blocks, download option is now available as the first option (#2198)
* Editor | Breakthrough style is renamed to Strikethrough (#2189)
* Editor | Show meaningful message when failed to open file by an existing application (#2163)  
* Sets | Gallery view might show object name twice (#2251)
* App | About-app screen should display Anytype user ID instead of profile object ID (#2172)
* Search | Search screen does not display updated search query when search results are missing (#2202)  
* Auth | Show/hide keyboard automatically when needed (#2207)
* Relations | When you create a new relation, default format is object now (#2196)
* Relations | Do not create new option if there is already an existing one with the same name (#2262)
* Relations | When you create a new relation and its name is too long, text should be ellipsized at the end (#2235)
* Dashboard | Do not crash when clicking twice on settings button (#2278) 
* Dashboard | When greeting message is too long, text should be ellipsized at the end (#2245)
* Settings | Do not crash when clicking twice on options (#2278)

### Design & UX 🔳

* App | Better keyboard animation starting from Android 11 (#2133)
* Editor | New fancy drag-and-drop behavior (#2246)
* Editor | All media blocks now support background styling (#2176)
* Editor | Improved sync status toolbar and header overlay behavior (#2170)
* Editor | TODO layout without cover had incorrect header height (#2169)  
* Editor | Code snippet design improvements (#2231)
* Editor | Fix corner radius for card-looking blocks (#2232)

## Version 0.6.1

### New features & enhancements 🚀

* Editor | Set cover for your object from Unsplash (#2141)
* Relations | Set target types for relations, whose values contain objects (#2127)
* App | Added logout warning (#2121)
* App | New app menu (#2119)
* Auth | Show screen about analytics during sign-up (#2122)
* Auth | Add logic for retry when failed to start account (#2124)

### Fixes & tech 🚒

* Editor | Fix app permission issues preventing users from uploading pictures and other media (#2143)
* Editor | Should close keyboard when closing a dialog screen with focused text input (#2152)
* Editor | Toggle block should expand / collapse in locked mode (#2116)

### Design & UX 🔳

* Sets | Do not draw offset column without content in grid views (#2129)
* Editor | Add-bookmark bottom sheet screen in dark mode (#2137)
* Editor | Style-color toolbar in light mode has incorrect background in selected tab (#2148)

## Version 0.6.0

### New features & enhancements 🚀

* Editor | Link appearance: customize how your links look on the canvas: choose from a simple text line, or a card, with optional icons. (#2087)
* Editor | Lock your objects to prevent accidental editing or enable read-only mode. (#2084)
* Editor | Create a bookmark from pasted URLs. (#2074)
* Editor | New logic for pressing the backspace key. (#2091)
* Dashboard | Changes in tabs are synchronized between peers, no longer requiring you to open/close an object to “refresh” the dashboard. (#2052)

### Fixes & tech 🚒

* Editor | Issues related to drag & drop sensitivity. (#2068)
* Editor | Incorrect numbers in numbered lists when sections group nested lists or blocks. (#2072)
* Editor | Progress bar and text should not overlap when a link-to-object block is in loading state (#2098)
* Editor | Title in the top toolbar is missing when your object has TODO layout (#2103)
* Sets | Enabled auto-correct when setting name for a new object. (#2067)
* Dashboard | An issue where removing objects from your favorites would not sync. (#2075)
* Search | Optimized object search. (#2095)

### Design & UX 🔳

* Editor | Image blocks in fullscreen mode are now optimized for light and dark mode. (#2094)

## Version 0.5.3

### New features & enhancements 🚀

* Editor | Link appearance: customize how your link to an object looks: line, card, with icon or without, etc. (#2087)
* Editor | Lock or unlock your page to prevent accidental editing (#2084)
* Editor | Create a bookmark from a pasted url (#2074)
* Editor | New logic for backspace press (#2091)
* Dashboard | Changes in tabs are synchronized between peers (#2052)

### Fixes & tech 🚒

* Editor | Issues related to drag & drop sensitivity (#2068)
* Editor | Incorrect numbers in numbered lists when there are nested lists or blocks are grouped by sections (#2072)
* Editor | Progress bar and text should not overlap when a link-to-object block is in loading state (#2098)
* Editor | Title in the top toolbar is missing when your object has TODO layout (#2103)
* Dashboard | Removal from favorites is not synced correctly (#2075)
* Sets | Enabled auto-correct when setting name for a new object (#2067)
* Search | Optimized object search (#2095)

### Design & UX 🔳

* Editor | When opening an image block in fullscreen mode, background is not adapted to the current mode (light or dark) (#2094)

## Version 0.5.2

### New features & enhancements 🚀

* Editor | Less misspellings: auto-correct is there in experimental mode (#2059)

### Fixes & tech 🚒

* Relations | When opening date-format value for a relation, then exiting to your device's home screen, then returning back to Anytype caused application crash (#2060)
* Sets | Fixes in Number, Date, and Checkbox filters (#2057)
* App | Create a new object via app shortcuts wouldn't work correctly if Anytype is already open in background (#2056)
* Tech | Fix missing file keys (#1353)

## Version 0.5.1

### New features & enhancements 🚀

* Relations | Apply target object types in filtering when searching for an object to set a relation's value (#2040)
* Objects and sets | Close objects and sets after moving to bin (#2038)
* Sets | Feature | Checking / unchecking tasks in Grid, Gallery, and List (#2042)

### Design & UX 🔳

* Sets | Fix tag and status colors for night mode for lists (#2030)

### Fixes & tech 🚒

* Editor | Failing to render objects containing at least one file block with embedded PDF (#2037)
* Editor | When trying to set a link to an object or to a web page for a text selection, the dedicated screen should not block the editor when the bottom sheet is hidden by swiping down (#2036)
* Home | Deleted items reappearing in favorites (#2045)

## Version 0.5.0

### New features & enhancements 🚀

* App | Introducing dark mode (#2018)
* App | Wallpapers (#1995)
* App | Offload your files from device (#1990)
* App | Reminding our users about saving the mnemonic phrase (#1982)
* Sets | Gallery view (#2009)
* Sets | List view (#1955)

### Fixes & tech 🚒

* Editor | Numbered block pattern triggered when not needed due to an incorrect regex pattern (#1987)
* Object | Should not create a link in your object to the newly created set (#1996)
* Sets | Should not crash when adding a file to relation (#2007)
* Sets | Relation "Name" should be available in sorts and filters (#1991)
* Relations | When trying to search for an object to add it to a relation, search does not work correctly (#1980)

### Design & UX 🔳

* Sets | Bottom navigation panel is there, at last! (#1988)
* Editor | Render object icon based on its layout type in mentions and links (#2003)
* Editor | New design for selected state (#1976)

## Version 0.4.4

### Design & UX 🔳

* Editor | Updated background for selected block (#1976)

### Fixes & tech 🚒

* Relations | Cannot add an object to a relation due to incorrect search result parsing (#1980)

## Version 0.4.3

### Design & UX 🔳

* Editor | Fix bullet icon and checkbox icon positioning relative to the text content (#1952) 
* Objects + sets | Redesigned set-cover screen (#1950)

### Fixes & tech 🚒

* Fix possible application crash during login and logout (#1954)

## Version 0.4.2

### Design & UX 🔳

* Redesigned authentication flow (#1928)
* Dashboard | Fix cards width (#1939)
* Dashboard | Add ripple animation for cards (#1942)
* New splash screen (#1940)

### Fixes & tech 🚒

* Editor | Should not crash when failed to parse an object's layout due to incorrect format (#1929)
* Editor | Should not crash when the target block is not found for the style panel (#1929)
* Editor | Should not crash when long-pressing empty space in the header when object layout has no title (#1930)
* Editor | Should not show a redundant toast with error message when opening user profile (#1935)
* Editor | Drag-and-dropping below or above a link to object always results in dropping inside this link (#1931)
* Editor | Undo/redo does not work correctly for relation values (#1932)
* Editor | Toggle block has incorrect font size (#1946)
* Sets | Should not crash when state is not initialized when displaying a view's filters (#1929)
* Sets | Should not display the change-object-type menu for sets (#1934)
* Sets | Should not prompt for object's name when creating an object with layout without title (#1938)
* Sets | Should not crash when relation state is not initialized (#1937)
* Sets | Support filters without any condition (#1933)
* Sets | Screen with filter conditions should dismissible by swipe (#1941)

## Version 0.4.0

### New features & enhancements 🚀

* Dashboard | Most wanted: select, delete objects or restore them from Bin (#1858)
* App | Smarter recycling: drafts without any history will be deleted after close (#1833)
* App | Resume whatever you were up to restoring the last opened object or the last opened set on application start (#1851)
* App | Displaying shortcut actions when clicking on your App's icon (#1888)
* App | On-the-go: introducing "Note" as a new type with a specific layout (#1852)
* Settings | Setup a default type for your Anytype. When creating a new object, your default type will be used (#1873)
* Editor | New markup: add links to objects, not only URLs! (#1893)
* Editor | Opening a file by default app on device instead of downloading it when clicked (#1904)
* Editor | Drag & drop (position above, below or inside) (#1848)
* Editor | Latex block in experimental mode. Let's hear your feedback! (#1814)
* Objects | Menu | Select a layout, which best suits your object: Note, Basic, Profile, or Task (#1894)
* Sets | When your object is open, you can navigate to set of your object's type or create a new set (#1880)
* Sets | You can now delete a relation from your set (#1890)
* Sets | Grid as fallback view: views not yet supported on Android can be seen as a grid (#1850)

### Design & UX 🔳

* Editor | More comfortable reading: increased the default font size (#1892)
* Editor | Undo-redo making a comeback! (#1913)

### Fixes & tech 🚒

* Sets | Status value missing due to incorrect value parsing (#1911)
* Sets | App crashes when opening URL from the relation in a browser and returning back to the App (#1911)
* Editor | Fix | App should not crash when undoing text color changes for numbered block (#1920)

## Version 0.3.2

### New features & enhancements 🚀

* Editor | Create a new relation via “/”-widget (#1829)
* Editor | Create date objects (today, yesterday, etc.) via @-mention (#1822)
* Editor | Titles of mentioned objects are no longer static. They are now synced (#1775)

### Design & UX 🔳

* Dashboard | Removed "Inbox" tab (#1824)

### Fixes & tech 🚒

* Editor | Enhanced: Better mime types recognition for file blocks (#1832, #1834)
* Editor | Fixed: should not show style panel along with keyboard when focusing a block, for which style panel was opened (#1830)
* Editor | Fixed: should not add empty space below when duplicating the last block in multi-select mode (#1837)
* Global search | Fixed: should close keyboard on exit (#1838)

## Version 0.3.1

### Fixes & tech 🚒

* Sets | Do not crash when opening an object contained in a relation (#1819)
* Editor | Open relation-value screen for all relations in featured relations block (#1820)

## Version 0.3.0

### New features & enhancements 🚀

* Editor | Our new fancy action bar will give you more power (#1755, #1781, #1782)
* Objects & sets | Favorite & unfavorite what you need to (#1792)
* Data View | No more secret calls. It’s now available for everyone! (#1780)
* Data View | Customise your Sets with cover images on desktop & mobile (#1747)
* Objects | You can now add or change a cover or icon by tapping it (#1779, #1783)
* Relations | You can now remove relations from your objects (#1741)

### Design & UX 🔳

* Editor | Ever close an object because you swiped the bottom sheet down? Well, that bug is gone! Bye-bye, bottom sheet swipe! (#1773)
* Editor | Create a link to an object via the “/”-widget (#1746)
* Editor | Enhanced “/”-widget triggering (#1761)
* Redesign | New main bottom toolbar (#1773)
* Redesign | New text-edit block toolbar (#1771)
* Redesign | Headers for objects and sets (#1744, #1749)
* Redesign | Move-to screen (#1739)
* Redesign | Link-to screen (#1746, #1751)
* Relations | Added format-specific placeholders (#1740)

### Fixes & tech 🚒

* Editor | Allow duplicating several blocks and support duplication for nested blocks (#1756)
* Editor | Improved file type/extension recognition for file block (#1810)
* Editor | Fixed: Capitalize when starting a new sentence (#1785)
* Editor | Fixed: Split-block logic for title block and description block (#1769)
* Editor | Open objects based on their layout for mentions and links (#1765)
* Dashboard | Sort results in “inbox” tab by lastModifiedDate (#1748)
* Objects | New objects now created by-default without icons (#1788)
* Data View | Fixed: Remove incorrect error message when deleting a view from dataview (#1805)

## Version 0.2.7

### New features & enhancements 🚀

* Relations | Added placeholders where relation value is missing (#1724, #1734)
* Slash widget | Using category title as filter will show all items from this category (#1735)

### Fixes & tech 🚒

* Editor | Fixed: Jumping cursor in description block (#1733)
* Editor | Fixed: @-character left in a text block triggers mention events when entering multi-select mode after reopening this object (#1731) 
* Editor | Fixed: Pressing "Action Go" on the keyboard in description block crashes the app if there is no other focusable text block below (#1725)
* Data View | Navigating to media objects (file, video, image) from sets (#1729, #1730)
* Data View | Fixed: Starting editing title for a set of objects in loading/initialization state would crash Anytype (#1738)  
* Relations | Fixed: Failing to navigate to an object from relation value (#1737)
* Relations | Fixed: Failing to find corresponding option meta data for tag or status would crash Anytype (#1738)

### Design & UX 🔳

* Data View | Layout fixes for object icons appearing in grid cells (#1732)

## Version 0.2.6

### New features & enhancements 🚀

* Global search | Filter archived objects (#1717)

### Fixes & tech 🚒

* Dashboard | Enhanced: Syncing / fetching an object's type ("Unknown type" issue) (#1723)
* Dashboard | Fixed: Incorrectly synced layout relation prevented user from opening an object (#1721)
* Dashboard | Fixed: Initial's letter sometimes overlaps an object's icon due to view recycling (#1722)
* Data View | Grid View | Enhanced object header syncing / applying granular changes (#1718)
* Relations | Fixed: Cannot navigate to task object from relation value (#1720)

### Design & UX 🔳

* Updated design for objects appearing in lists (#1716)

## Version 0.2.4

### New features & enhancements 🚀

* Editor | @-mention Sets in-line (#1709)

### Fixes & tech 🚒

* Object search | Sort results by date last opened (#1710)
* Object | Fixed: Failure to render an object would result in a white screen due to a regression introduced in 0.2.3 (#1711)
* Object | Fixed: Adding a new relation to an object due to a regression introduced in 0.2.3 (#1711)

### Design & UX 🔳

* Updated design for objects appearing in lists (mentions, link-to, move-to, etc.) (#1697)

## Version 0.2.3

### Fixes & tech 🚒

* Data View | Icon is not synced correctly with other peers (#e6eef8d)
* Data View | Fix create-new-record flow (#1707)
* Object | Remove featured relations max count limit (#1706)
* Object | Do not show featured relation with empty values below object's header (#1706)
* Editor | Code block | Updated syntax rules for Rust (highlight basic comments) (#1708)

## Version 0.2.2

### Fixes & tech 🚒

* Object | Opening styling panel in objects with task layout crashes the app (#1701)
* Object | Opening search for content in objects with task layout crashes the app (#1701)
* Data View | Opening filter without condition in data view crashes the app (#1704)
* Data View | Click on filter with hidden relation in data view crashes the app (#1702)

## Version 0.2.1

### New features & enhancements 🚀

* Editor | Added basic syntax rules for Rust (#1695)

### Fixes & tech 🚒

* Relations | Object | Fix navigation to sets (#1698) 
* Data View | Should set new active view after current one is deleted (#1696)
* Data View | Do not allow creating new records if data view is not initialized (#1698)

## Version 0.1.14

### Fixes & tech 🚒

* Fix bookmark image loading (#1570)

## Version 0.1.13

### Fixes & tech 🚒

* Fix split title issue (#1572)
* Do not crash when failed to parse path for profile image (#1574)
* Do not crash when failed to parse layout for dashboard link (#1574)
* Fix loading state inconsistencies on dashboard (#1575)

## Version 0.1.12

### Design & UX 🔳

* Redesigned dashboard (#1538, #1545)
* Do not focus an empty document's title on start if the title is not empty (#1559)

### Fixes & tech 🚒

* Do not crash when failing to parse path for avatar image during sign-up flow (#1544)
* Do not crash when opening action menu for archived page without icon (#1544)
* Do not crash when navigating back in case of navigation stack containing more than one copy of the same document (ScreenA-ScreenB-ScreenA scenario) (#1541)

### Sets & relations 📚

* Navigate to objects from data view or relation-value screens (#1539)
* Show the Android keyboard reliably for text-based relations with empty value (#1542)
* Do not exit edit mode when deleting one of the values from relation's file list (#1543)
* Data view pagination (#1561)

## Version 0.1.11

### New features & enhancements 🚀

* New slash widget: boosting content creation (#1524)

### Design & UX 🔳

* New style toolbar: changing text style on the fly (#1529)
* New markup toolbar (#1421)
* New set-link-as-markup toolbar (#1424)
* Description block (#1414)

### Fixes & tech 🚒

* Show soft input when focusing in filter in navigation screen (#1475)
* Show soft input when focusing search-on-page input field (#1531)
* Search in emoji picker works again 🙏💕👉😌🔥🤔 (#1473)

## Version 0.1.9

### Fixes & tech 🚒

* Fix Firebase crashlytics core dependencies (#1419)

### Middleware ⚙

* Updated middleware protocol to `0.15.1` (#1420)

## Version 0.1.8

### New features & enhancements 🚀

* You can leave your mnemonics on your desktop and login with a QR code (#1380)
* Fixed behavior when choosing default text/background color for a text range has no effect on the text block style (#1387)

### Design & UX 🔳

* Title in the Page menu should be one-lined or ellipsized at the end (#1415)

### Fixes & tech 🚒

* Adding files from device's recent files menu will no longer crash Anytype (#1381)
* App supports two build variants: stable and experimental  (#1391)

### Middleware ⚙

* Updated middleware protocol to `0.15.0`

## Version 0.1.7

### Middleware ⚙

* Updated middleware protocol to `0.14.7`

## Version 0.1.6

### New features & enhancements 🚀

* Introduce page's cover. First iteration does not support image repositioning (#1220)
* Refactored turn-into operations (#1115)

### Middleware ⚙

* Updated middleware protocol to `0.14.2` (#1245)

## Version 0.1.5

### New features & enhancements 🚀

* Displaying sync status for documents (#1188)
* Encrypted storage for sensitive data (#1189)

### Design & UX 🔳

* Redesigned document's menu (#1188)
* Move undo/redo buttons on top toolbar (#1188)

### Fixes & tech 🚒

* Event processing: switched from `BroadcastChannel` to `SharedFlow` (#1180)

## Version 0.1.4

### New features & enhancements 🚀

* When entering into scroll-and-move mode via action menu, exit into editing mode, not multi-select mode (#1126)
* Show soft input when opening global-search screen (#1120)
* Instead of navigating to dashboard, return to editor mode when pressing back button in multi-select mode (#1106)

### Design & UX 🔳

* Remove redundant space from header block's background (#1127)

### Fixes & tech 🚒

* Do not crash while DND on home dashboard if dropped item's index is invalid (#1111)
* When failed to fetch account after logging in with mnemonic phrase, return to keychain-login screen (#1112)
* Do not crash when failed to normalize url in markup (#1113)

### Middleware ⚙

* Updated middleware protocol to `0.14.1` (#1110)

## Version 0.1.3

### New features & enhancements 🚀

* Show loading state when referenced documents are being synced on dashboard, inside a page or in mention (#1072)
* Start scrolling-and-moving a block via action menu (no longer need to enter multi-selection mode in order to scroll-and-move a block) (#1055)
* Added loading state for logout operation (#1099)

### Fixes & tech 🚒

* Should save the latest text input if you close the page quickly (#1067)
* Keyboard won't show up in code snippet on Android 7 (#1024)
* Do not crash if document's title is broken after paste (#1108)

### Middleware ⚙

* Updated middleware protocol to `0.13.26` (#1100)

## Version 0.1.2

### New features & enhancements 🚀

* Code-snippet syntax highlighting. Our first iteration supports native syntax highlighting for Kotlin, Javascript, Go, Python, Typescript, JSON, and CSS. (#989)
* When creating a new page via mentions, the new page's name will be the text typed afterwards. (#994)
* When creating a new list item, it will inherit style properties from the previous list item after pressing the return key (#1017)
* Enabled search-on-page for profile document (#1027)
* Search text through media blocks (bookmark, link, file) (#1008)
* Users can change a code block's background colour (#1013)

### Fixes & tech 🚒

* Should not delete a text block's style properties when creating a mention inside this block (#699)
* When the checkbox is checked, all text should have the same color, even if this text block has markup (#833)
* When switching from reading mode to edit mode, create-mention trigger does not work (#1037)
* When copying a text block to the clipboard, style properties are now also copied (#1015)
* It's impossible to open the linked text just after adding a link to this text (#883)
* When navigating to a document via the navigation-structure screen, open this new document without passing by dashboard-screen (#1010)
* Prevent from moving the link into the document that this link is pointing to (#1025)
* Fix soft input visibility/focusing issues on Android 7.1 (#1029)
* Keyboard won't show up in code snippet (#1024)
* When changing image block's indentation, image is not scaled correctly (#1021)
* Setup for the upcoming login-with-QR-code feature (#822)

### Middleware ⚙

* Updated middleware protocol to `0.13.22` (#1045)

## Version 0.1.1

### New features & enhancements 🚀

* Search on page. Search through text blocks (except code block) on page (#990)
* Images can be full screen when tapped, allowing users to zoom in (#968)
* Disable animation for edit-mode in order to increase editor performance (#884)
* Should close style toolbar (instead of closing document) when back button pressed (#973)
* Create a new line inside code snippet on enter press (#970)
* Integrate three dots divider (#978)

### Design & UX 🔳

* Navigation icons updated (#992)

### Fixes & tech 🚒

* Survive process death and restore screen state when app returns from background (#985)
* When scroll-and-move is enabled, should move blocks according to document order, not selection order (#971)
* Emoji cross-platform sync issues (#969)
* Fix soft input visibility/focusing issues on Android 7 (#966)
* Change min sdk to Android 24 (#976)
* Cannot set carriage into an empty text block in large documents (#906)
* When changing icon for a document, link block should not have overlaying images (#890)
* Should show a correct error message when trying to move a block on the same position (#1007)

## Version 0.1.0

### New features 🚀

* Allow using code block in multi-select and scroll-and-move modes (#892)
* Enable mentions for all text blocks (except code block) (#939)
* Create both a new page and a mention pointing to this new page via mention bottom sheet (#631)
* Allow closing bottom sheet with mention suggests by pressing back button (#631)

### Design & UX 🔳

* Orange color as new accent color (#956)
* New design for add-link-markup bottom sheet (#774)
* Changed object names in add-block and turn-into panels (#752)

### Fixes & tech 🚒

* Prevent user from archiving profile document (#962)
* Show toast message when editor's content is copied to in-app clipboard (#958)
* Prevent view without focus from gaining focus on long click (#954)
* Support and render title as simple text block (#799)
* Url markup should have the same color as text block's text color (#818)
* Hiding keyboard via bottom toolbar just after typing results in losing text on screen (#953)
* Removing image from document results in broken set-icon-flow logic (#951)
* Set default text color if value is not present when processing text color's granular change (#946)
* Include checked/unchecked changes in granular-change-update mechanism (#945)
* New state handling for errors (observable subject). Show error only once (#943)
* Remove flickering effect when opening navigation screen (#941)
* Disable Crashlytics crash reporting for debug builds (#940)
* Should focus the last empty text block when clicking under document's blocks (#935)
* Clicking on empty space before document is loaded should not crash application (#930)
* Focusing on start may crash application by some users (#931)
* Send analytics for popup screens and button click events (#592)
* Prevent data racing issues while calculating the diff between two lists on home dashboard (#933)

### Middleware ⚙

* Updated middleware protocol to `0.13.20` (#851)

## Version 0.0.49

### Fixes & tech 🚒

* Render emoji apple icon in page top toolbar, fallback to system emoji in case of exception (#926)
* Disable DND for profile header on home dashboard (#923)
* Exclude text color and background marks where param value is equal to default value (#786)
* Try/catch instead of crashing for issues from 0.0.48 (#925)

## Version 0.0.48

### New features 🚀

* Automatically select the first loaded account when signing in (#869)

### Design & UX 🔳

* Indent aware scroll-and-move (#820)
* Enter multi-select mode via document's main menu (#896)
* Redesigned multi-select bottom panel (#872)
* Redesigned markup url-link bottom sheet (#774)
* Other design\layout fixes (#870)

### Fixes & tech 🚒

* Profile's empty name results in app's crash (#905)
* Update application package (#917)
* New config for Crashlytics for release project (#917)
* Invalidate incorrect ranges for markup (#908)
* Amplitude analytics for basic events (#592)
* Block-merge operations for documents containing sections (aka divs) (#912)
* App crashes when opening action menu for link block, which was created by turning a text block into a page (#910)
* Should create a new toggle on enter press at the end of the non-empty toggle block (#907)
* Should convert toggle block to paragraph on enter press if toggle block's text is empty (#886)
* When creating a new document and focusing its title, cursor should be visible (#903)
* Should not crash Android client when changing media block's background color on Desktop client (#814)
* Stretched background cover affects app's performance on home dashboard screen (#901)
* Remove Archive from Navigation links (inbound, outbound) (#919)
* Links on home dashboard sometimes disappear behind the center of the screen (#829)

### Middleware ⚙

* Updated middleware protocol to `0.13.13` (#851)

## Version 0.0.47

### New features 🚀

* Archive (#547)
* Link to existing object (#770)
* Move-to from one document to other document (#770)
* Invite code screen (#772)
* Allow user to add a block below via action menu (#771)

### Design & UX 🔳

* New cover for home dashboard (#839)
* Enhanced action-menu animation + background blur (#812)

### Fixes & tech 🚒

* Show alert dialog when failing to open a document (#823)
* Enhanced split-block operations (#731)
* Design fixes (#806)
* Action menu fixes (#665)
* Safely setting text color and background color (#858)
* Handle exceptions when emojifier fails to provide uri for emoji icon (#856)
* Render children for all text blocks (#846)
* Scroll-and-move restrictions issues (#847)
* Cannot add block after document's title via add-block-menu (#827)
* When navigating to a document via search-screen, open this new document without passing by dashboard-screen (#830)
* Inconsistent logic when adding markup in certain corner cases (#509)
* If you change checkbox's text color and then check off this checkbox, its text color always becomes black whereas it should have the color that you've set before (#785)

### Middleware ⚙

* Updated middleware protocol to `0.13.8` (#851)

## Version 0.0.46

### New features 🚀

* Mentions are rendered with images and emojis (#658)
* When adding new block via add-block screen, should replace current text block instead of adding a new block after this text block if this text block is empty (#325)

### Fixes & tech 🚒

* Prevent text block from gaining focus when opening its action-menu on long click (#776)
* Refactor navigation toolbar state handling inside `ControlPanelMachine` (#792)
* Remove turn-into action from page block's action menu (#787)
* Should change number color when changing numbered block's text color (#797)
* Document's image icon (uploaded from device's gallery) isn't visible in the mention suggester (#789)
* App crashes on setup-selected-account screen due to incorrect icon id (#739)
* Divider block should be selectable in multi-select and scroll-and-move mode (#778)
* Remove legacy selection param from `ControlPanelMachine` (#795)

## Version 0.0.45

### New features 🚀

* Test flight for turn-into restrictions in edit-mode and multi-select mode (#376)
* Styling panel | Switching between block-mode and markup-mode based on selection changes (#594)

### Design & UX 🔳

* Apple emojis or uploaded image in block-action-toolbar link's preview (#630)
* Uploading state for upload-image-for-document flow (#765)
* Update Graphik font file with the official one (#768)
* Navigate from context menu to styling panel (#594)

### Fixes & tech 🚒

* Removed archived pages from mention suggester and search results (#766)
* Checkbox state in action-menu preview is not synced with editor state (#748)
* User interactions with checkbox button are not synced correctly with the middleware (#749, #668)
* When searching for pages, if filter text is empty space, query returns only pages where title contains empty spaces (#746)
* Regression. Text is not always set when creating a lot of text blocks (#741)
* Respective theme colors should differ for text color and background colors in action menu (#738)
* Inconsistent DND-behavior on dashboard due to incorrect drop-target position calculation (#657)
* Fix app configuration lifetime (#735)
* Avatar image is not displayed after registration started after logout (#692)
* Editor business logic (event detection for backspace and enter press, checkbox button click detection, etc.) is broken for text blocks, whose style was changed via `turn-into-toolbar` in `multi-select` mode after returning to `edit` mode (#514)
* Should not show toast when clicking on markup url (#698)

## Version 0.0.44

### New features 🚀

* Download media (video and images) (#681)
* Turn-selected-block(s)-into-page(s) in edit and multi-select mode (#671)

### Design & UX 🔳

* Design fixes pack (profile, search, style toolbar, bookmark block, block icons on add-block/turn-into toolbar, etc.) (#602)
* Fix action toolbar constraints (#611)

### Fixes & tech 🚒

* Refactoring | Decomposed monolithic `BlockViewHolder`: DRY, better inheritance and composition (#645)
* Scroll-and-move restriction: prevent from moving parent into child (#696)
* Dot is missing after number in numbered block when its number and indent gets updated by payload change (#704)
* Nested block on-backspace-pressed deletion issues (#697)
* Library/framework updates (#687)
    * AAC `Navigation`,
    * AndroidX `Core`, `ConstraintLayout`, `RecyclerView`, `Fragment`, `Lifecycle`
    * Kotlin `DOKKA`
    * `Protobuf` Gradle plugin
    * Firebase `Crashlytics` Gradle plugin
* Updated `Kotlin` (`1.4.0`) and `Coroutines` (`1.3.9`) (#682)
* Image size issues (#648)
* Toggle's button stops working when switching from multi-select to edit mode (#643)
* Removed task-block-related legacy (#679)
* Removed "Color", "Background" actions for media blocks (#611)
* Removed "Add Caption", "Replace", "Rename" actions (#611)
* Render-state syncing for all text blocks (#719) 

## Version 0.0.43

### New features 🚀

* Support block indentation for paragraphs, checkboxes, bulleted and numbered lists (#617)
* Scroll-and-move restrictions (#616)

### Design & UX 🔳

* Changed main bottom-toolbar background (#660)
* Added app icon (#596)

### Fixes & tech 🚒

* Render-state syncing (from GUI to VM, from VM to GUI) refactoring (#663) 
* After updating document's image, this image is only updated after reopening the document (#642)
* Event subscription lifecycle issues (#675)

## Version 0.0.42

### New features 🚀

* Mention suggests (#574)
* Editable mentions (#573)

### Design & UX 🔳

* New design for scroll-and-move targeting (#636)
* Enhanced scroll & move targeting (#636)

### Fixes & tech 🚒

* Turn off custom context menu (#594)
* Should not crash the app when opening action menu for currently focused block (#635)
* DI/Dagger optimizations (#626)
* Refactored `ControlPanelMachine` (#634)
* Updated Dagger to `2.28.3` (#627)
* Safely setting emoji icon on home dashboard and in editor if image not found in our data set when searching by unicode (#638)
* Show error if we failed to start account while sign-in (#633)

### Middleware ⚙

* Updated middleware protocol to `0.13.0` (#454)

## Version 0.0.41

### New features 🚀

* Setting random emoji icon when creating a new page (#603)

### Design & UX 🔳

* Enhanced scroll & move targeting (#610)
* Redesigned block toolbar (#590)
* Redesigned keychain dialog screen (#614)
* Checked and fixed line-spacing values for text blocks in editor (#614)

### Fixes & tech 🚒

* Turn-into in multi-select mode should not break selected/unselected-state-related logic (#621)
* App should not crash when user presses change-style button or open-action-menu button on block-toolbar when document's title is focused (#620)
* Drag-and-drop area issues on home dashboard (#570)
* Should not navigate to congratulation screen (designed for sign-up flow) after sign-in (#606)
* Setup analytics module (#618)

### Middleware ⚙

* Updated middleware protocol to `0.12.2` (#454)

## Version 0.0.40

### New features 🚀

* Test flight for scroll-and-move feature (#567)

### Fixes & tech 🚒

* Added new custom span for rendering mentions in text blocks (#563)
* Added new text watcher for intercepting mention-related events (#574)

## Version 0.0.39

### New features 🚀

* Search-document-engine screen integrated on home dashboard and pages (#555)
* Picking/removing avatar image for profile document (#568)
* Setting links via block-styling toolbar (#559)

### Design & UX 🔳

* New animation for action menu (#464)
* Redesigned avatar and greeting text sizes and relative positioning on home dashboard (#571)
* Redesigned add-block/turn-into bottom sheet (new palette, updated categories) (#572, #569)

### Fixes & tech 🚒

* Should not crash app when opening the archive from the workspace-navigation-structure screen (#582)
* Should not crash app when opening _link to_ tab on workspace-navigation-structure screen (#576)
* When creating a new nested page (B) inside some other page (A), the link block for the page B should be present on the page A when user navigates back to the page A (#561)

## Version 0.0.38

### New features 🚀

* New workspace-navigation screen integrated for home dashboard and pages (#552, #553, #556)

## Version 0.0.37

### New features 🚀

* Editable profile document (#504)
* New emoji search engine in document-icon picker (#549) 

### Design & UX 🔳

* Using Apple emojis as document icons (#542)
* Redesigned profile screen (#504)

## Version 0.0.36

### New features 🚀

* User can set image icon for document by choosing an image from device (#535)
* Styling toolbar shows currently applied style in markup-styling mode (#525)
* Wired document's icon with action menu (#529)
* User can upload files from device's cloud (#537)

### Design & UX 🔳

* Redesigned page emoji icon picker (#531)
* Empty state (zero blocks selected) for multi-select mode (#527)
* Uploading state for 

### Fixes & tech 🚒

* Fixed file permission issues on Android 10 and 11 (#334)

## Version 0.0.35

### New features 🚀

* Custom markup context menu enabled by default. Test flight (#483)
* Styling toolbar shows currently applied style in block-styling mode (#503)

### Design & UX 🔳

* Second iteration for custom markup context menu: y-positioning (relative to text), button states (#483)
* Inter (regular, medium, bold) is now the main font in the editor (#522)
* Redesigned selected states for tabs in styling toolbar (#506)
* Redesigned selected states for markup and alignment in styling toolbar (#506)
* Redesigned selected states for background and text color tabs in styling toolbar (#506)

### Fixes & tech 🚒

* Support suggestions for custom keyboards (#466)
* Should ignore split-line `enter` press in document's title (#513)
* Setting cursor when pasting from anytype clipboard (#484)
* Should focus document's title when first paragraph (as the first block in the document) is deleted (#498)

## Version 0.0.34

### New features 🚀

* Enabled markup for headers and highlight blocks (#480)

### Design & UX 🔳

* New screen for debug settings (#492)
* Custom context menu. First iteration available only in debug mode (#430)

### Fixes & tech 🚒

* Enabled markup links (#200)
* Added UI and integrations tests for basic CRUD, split and merge operations in editor (#497)
* Better control over cursor position while CRUD, split and merge operations in editor (#491)
* Fix incorrect cursor positioning while deleting an empty block (#493)
* Fix Inconsistent behavior when merging two highlight blocks (#478)
* Should preserve text style while splitting (#479)
* Should focus and open keyboard when creating headers or highlight block (#485)

## Version 0.0.33

### New features 🚀

* Select text and copy-paste inside Anytype. First iteration (#467)
* Copy and paste multiple blocks in multi-select mode. First iteration (#467)

### Design & UX 🔳

* Undo/redo migrated to document's context menu (#461)

### Fixes & tech 🚒

* Resolve race conditions on split and merge (#463, #448)
* Turn-into code block in edit-mode and multi-select mode does not work (#468)

## Version 0.0.32

### New features 🚀

* User can paste from web to Anytype. First iteration (#447)
* Turn-into in multi-select mode for text blocks (#458)
* All media blocks can be selected in multi-select mode (#427, #428)

### Fixes & tech 🚒

* New and more stable enter-press detection (#449)
* Refactored media block click handling (#427, #428)
* Load profile picture from local http-server instead of loading image blob (#431)
* Should persist link markup while editing text (#455)
* Regression | Should convert an empty list block to a paragraph on enter-pressed event (#457)
* Inconsistent backspace detection when user presses backspace on non-empty text where selection > 0 (#450)

### Middleware ⚙

* Updated middleware to `0.11.0` (#454)

## Version 0.0.31

### New features 🚀

* User can add code block (#409)

### Design & UX 🔳

* New bookmark block design (#422)
* Render bookmark in multi-select mode (#422)
* Updated subtitles for add-block or turn-into bottom sheet items (#429)
* Text background should have the same height as the OS text-selection highlight (#392)
* Text background should have z-axis priority lower as the one of the OS text-selection highlight (#426)

### Fixes & tech 🚒

* Migrate from short name emojis to unicode when parsing document icons (#408)
* Consuming event payload from middleware callaback responses (#408)
* Hard-coded alpha invite code for internal use (#408)
* `PageViewModel` refactoring (#408)
* Better logging for middleware requests and responses (#421)
* Should persist home dashboard document order (#425)
* New way to render background mark: using `Annotation` span instead of `BackgroundColorSpan` (#436)

### Middleware ⚙

* Updated middleware to `0.9.0` (#339)

## Version 0.0.30

### New features 🚀

* Multi-select mode: user can enter/exit this mode, select and delete blocks (#404).
* Multi-select mode: turn-into (not stable) (#375)
* Enable action toolbar for media blocks (#405)

### Design & UX 🔳

* Multi-select toolbar (top and bottom) ($404)
* Basic animations on entering/exiting multi-select mode (#404)
* Added background state selector for editor blocks (#404)
* Padding and margin fixes for editor blocks ($404)

### Fixes & tech 🚒

* Should render multi-line text in action toolbar block preview (#405)
* Action toolbar supports text color and background color (#405)
* Action toolbar has its own layouts (for a better separation of concerns) (#405)
* Do not crash app wheh failing to parse bookmark uri (#414)
* Migrate from Fabric to FirebaseCrashlytics (#414)
* Read/edit mode switcher for editor (#404)
* Refactored top navigation bar in document (switched to custom widget implementation) (#406)

## Version 0.0.29

### New features 🚀

* Navigation from bookmark block to device browser (#390)
* New block-action toolbar enabled for all text blocks (#382)

### Design & UX 🔳

* Text block previews in the new block-action toolbar now have the same style as in the editor (#382)
* Add-block toolbar should have its title hidden while scrolling (#374)
* Block-styling toolbar in block styling mode (applying text color, background to the whole block) (#379)
* Enabled style page features in block-styling toolbar (#379)
* Fixed collapsing toolbar animation on home-dashboard screen (#384)
* New `turn-into` toolbar (#386)
* Skip collapsed state for bottom sheet dialogs (`add-block`, `turn-into`) (#391)
* Ellipsize and reduce bookmark's description to two lines, bookmark's title to one line (#390)

### Fixes & tech 🚒

* Should open keyboard and focus the target when a new block is added to the document (#388)
* Should close keyboard after document archiving (#395)
* Should hide archived documents from home dashboard screen (#387)
* Fix carriage positioning for `split` / `merge` operations (#353)
* Main layout optimization (switched to `FragmentContainerView`) (#385)
* Refactored custom context menu for text blocks (#393)
* Title and emoji for inner document link's title and emoji icon taken from details (#389)

## Version 0.0.28

### Design & UX 🔳

* New block-styling toolbar with swiping pages (enabled only for markup/selected text editing) (#366)
* New block-action toolbar (enabled only on paragraph blocks) (#366)
* New main toolbar with options: `add-block`, `multi-select` (disabled), `remove-focus` (#370)
* New behavior for create-new-page (+) button on editor screen: (+) button is hidden while scrolling (#377)
* Skipping `collapsed` state while closing page bottom sheet (#377)

### Fixes & tech 🚒

* Updated Kotlin to `1.3.72` (#378)
* Switched from hex color codes to named colors (#377)
* Refactored markup-related spans to implement custom interface (better control over removing spans from text while updates) (#377)

## Version 0.0.27

### Design 🔳

* Rendering bookmark in error / failed-to-load state (#351)
* New markup menu (instead of bottom toolbar) (#348)
* Emoji picker issues (alignment, empty spaces, etc) (#324)

### Fixes & tech 🚒

* Get title and icon from document details (#356)
* Wire bookmark menu with action toolbar (#351)
* Add-block bottom sheet has incorrect fonts in title and subtitle (#361)
* Do not show main toolbar when no block is focused on a page (#103)
* Create the block when user taps under all types of non-empty blocks (#350)
* Duplicate action should transfer the carriage to a new block (#352)
* Duplicated-platform-classes issue caused by `emoji-java` library breaks Github Actions CI (#357)

### Middleware ⚙

* Updated middleware to `0.5.0` (#339)
* Added `blockSetDetails` command (#339)

## Version 0.0.26

### New features 🚀

* Navigation to desktop from any page on bottom-swipe gesture (#316)
* Hot keys for patterns (`bullet`, `numbered`, `h1`, `h2`, `h3`, `quote`, `toggle`, `checkbox`, `divider`)  (#340)

### Design 🔳

* Redesigned add-block panel (using new bottom sheet design) (#329)
* Design fixes for list-item blocks (alignment, padding, etc.) (#328)
* Update checkbox's text color in checked-state (#328)
* Image block scaling-related fixes (#326)

### Fixes & tech 🚒

* Should focus title block after page creation (#323)
* Should close keyboard when exiting page via toolbar's back button (#338)

## Version 0.0.25

### New features 🚀

* Undo/redo changes in document (unstable) (#284)
* User can archive documents (#293)

### Design 🔳

* Added navigation bar with title and icon for pages (#293)

### Fixes & tech 🚒

* Should open new page after its creation on some other page (#283)
* Should update link block titles when corresponding page titles have been updated (#283)
* Should set "Untitled" as link's title if it's not set or blank (#283)

### Middleware ⚙

* Added `blockUndo` command (#284)
* Added `blockRedo` command (#284)
* Added `blockSetPageIsArchived` command (#293)

## Version 0.0.24

### New features 🚀

* User can add bookmark placeholder and create bookmark from url (#140)
* User can add image blocks (#139)
* User can add file blocks (#295)
* User can add toggle blocks and change expanded/collapsed state (#313)
* Added support for nested blocks rendering (#313)

### Fixes & tech 🚒

* Toolbars should not prevent user from scrolling page to its end (#310)
* Should create a new block after the target block when user adds a new block via add-block toolbar (#305)
* Refactored block creation in `Middleware` and reduced code duplication (introduced factory to create a block from a block prototype) (#140)
* New mappers (from middleware layer entity to data layer entity) (#140)
* Introduced new rendering converter (from business tree-like data structures to flattened view data structures) (#313)

### Middleware ⚙

* Added `blockBookmarkFetch` command (#140)
* Added `blockUpload` command (#295)

## Version 0.0.23

### New features 🚀

* Bookmark block rendering (#290)
* User can add video blocks (#142)
* User can watch video from video blocks (#142)

### Fixes & tech 🚒

* Refactored create-block requests (#142)

## Version 0.0.22

### New features 🚀

* User can download files on phone (#256)
* User can set an emoji as page icon (#280)

### Fixes & tech 🚒

* Update Kotlin to 1.3.70 (#278)

### Design 🔳

* Fix home dashboard list item spacing (#258)
* Different icons for different mime types for file blocks (#288)
* Page icon picker (#280)

### Middleware ⚙

* Added `blockSetIconName` command (#280)

## Version 0.0.21

### New features 🚀

* Render file and picture blocks (#255)

### Design 🔳

* Added page-icon-picker widgets: layout, adapters, etc. (#243)

### Fixes & tech 🚒

* Should hide keyboard when closing a page (#263)
* Fixed emoji transparency issue (#261)
* New models for files in `domain` and `data` modules + mappers (#269)
* Provide config object for the whole app (#272)
* Added `UrlBuilder` for building urls for file and pictures (#272)
* Updated middleware config model to include gateway url (#270)

## Version 0.0.20

### New features 🚀

* Allow users to split blocks (not stable yet) (#229)
* Allow users to set background color to block layouts (#244)
* Allow users to create new pages on home dashboard by pressing (+) button on page screen (#191)
* Allow users to add divider blocks (#234)
* Enable sub-page navigation (naive implementation) (#235)
* Implemented new back navigation: closing pages on swipe-down gesture (#231)

### Design 🔳

* One-line (ellipsized) page titles on home dashboard screen (#233)

### Fixes & tech 🚒

* Turn-into panel is still visible when system opens virtual keyboard on focus (#169)
* Missing diff-util implementation for headers results in app crash (#227)
* Option toolbars (`add-block`, `turn-into`, `color`, `action`) are still visible when system opens virtual keyboard on focus (#102)
* Default text color and default background color from app ressources aren't converted correctly to hex color code (#204)
* Added scenarios for UI-testing (#241)

### Middleware ⚙

* Added `blockSplit` command (#229)
* Added `blockSetTextBackgroundColor` command (#244)

## Version 0.0.19

### New features 🚀

* Allow users to create numbered lists (nested lists are not supported) (#156)
* Allow users to create a sub-page (navigation is not supported) (#214)

### Fixes & tech 🚒

* Fix: Text watcher is not always removed when the corresponding block is deleted (#221)
* Testing: added basic unit testing for BlockAdapter (#220)
* Testing: added first UI tests for editor/page (#208)

## Version 0.0.18

### New features 🚀

* Merge the target block with the previous block if the carriage of the target block is positioned at the beginning of the text on backspace-pressed event (#159)
* Turn a list item into a paragraph on empty-block-enter-pressed event (#207)
* Enable keyboard/code (not stable yet) (#80)

### Fixes & tech 🚒

* Improved `BlockViewDiffUtil` implementation (better change payload procession) (#164, #195)
* Page titles on the home dashboard are not always updated when user returns back from a page to the home dashboard (#199)
* Inconsistent behaviour while editing page's title on page screen (#182)
* Event channel refactoring (decreased code duplication) (#194)

### Middleware ⚙

* Added `blockMerge` command (#159)

## Version 0.0.17

### New features 🚀

* User can now use the color toolbar to change the text color of the whole text block (#153)
* User can now use the markup-color toolbar to change the background color of the selected text (#111)
* Create a checkbox-list item on enter-pressed-event (instead of a simple paragraph) (#155)
* Create a bulleted-list item on enter-pressed-event (instead of a simple paragraph) (#154)
* `Block.Content.Text` model now has optional `color` property (#153).
* Added documentation engine (`DOKKA`) for `domain` module: documentation is generated automatically from KDoc (#168).
* Added new content model: `Block.Content.Link` (#173)

### Design 🔳

* Updated app fonts (#183)
* Removed shadows from cards (#177)

### Fixes 🚒

* User cannot undo markup formatting if there are already several markups of the same type in the text (#151)
* Markup is broken when user splits the range (#122)
* Page title changes are not saved after user pressed backspace on empty page title block (#185). 

### Middleware ⚙

* Updated middleware library and protocol to 0.2.4 (#173, #181)
* Added `blockCreatePage` command (#173)
* Added `blockSetTextColor` command (#153). 
* Added `accountStop` command (#180)

## Version 0.0.16

### New features 🚀

* Added turn-into toolbar: allow user to change block text style (#144)
* Clearing focus when user hides keyboard (#133)
* Added `PLUS` button on page screen (#133)
* Better UX: increased cursor/focusing speed (cursor is now moved to the next block with a greater speed) (#135)

### Fixes 🚒

* Fixed main toolbar visibility illegal states: no longer showing the main toolbar when no block is focused (#103)
* Wire the control panel with the focused block: `ControlPanelToolbar` holds the id of the focused block (#133)
* The main toolbar is hidden when no block is focused on a page (#103)
* Fixed a regressed issue: new paragraph is not focused when created after on-enter-press event is triggered (#138)
* Fixed Github Actions CI issue: using token from repository secrets (#148)
* Fixed issues related to incorrectly calculated adapter position resulting in app crash (#147)

### Middleware ⚙

* Refactored event handler (list of events is now processed at once, not one event after another as before) (#134)
* Added `blockSetTextStyle` command. 

## Version 0.0.15

### New features 🚀

* Enabled user to add `checkbox` and `bullet` blocks. (#106)
* Allow user to `delete` (unlink) or `duplicate` blocks inside a page (#107).
* Added block-action (delete, duplicate) toolbar (#107)
* Create a new paragraph on enter-press event at the end of the line (#129)
* Create an empty paragraph block when user clicks on empty space (#126)
* Delete target block when user presses backspace inside an empty block (#113)

### Fixes 🚒

* Every page is now opened in expanded state (#121)
* Should not show colour toolbar and add-block toolbar at the same time (#119)

### Middleware ⚙

* Added support for `duplicate` and `unlink` operations (#107)
* Middleware-client refactoring (#118)
