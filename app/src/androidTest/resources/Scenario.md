### Testing scenarios

- [x] create a page with different content, simulate event flow, where this page is received via event channel, check that the created page is rendered.
- [x] create a page with one paragraph, focus this paragrah and start typing some text, wait and then check that after re-rendering we have the correct state with the new text.
- [x] create a page with title, focus this title, then check if the block toolbar is visible
- [x] when a block is focused and user clicks hide-keyboard button, then focus should be cleared and all toolbars should be hidden
- [x] simulate block split behavior by mocking appropriate events
- [ ] create a page with one title and one paragraph, simulate a new paragraph creation by mocking appropriate event flow
- [ ] create a page with one title and one paragraph, simulate event flow where this paragraph should be deleted
- [x] create a page with one title and two paragraph, simulate event flow where divider block is added after first paragraph