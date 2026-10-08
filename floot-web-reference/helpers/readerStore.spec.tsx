import { readerStore } from "./readerStore";

class MemoryStorage {
  values = new Map<string,string>();
  getItem(key:string){ return this.values.get(key) ?? null; }
  setItem(key:string,value:string){ this.values.set(key,value); }
  removeItem(key:string){ this.values.delete(key); }
}

describe("readerStore", () => {
  it("resets only a corrupted storage block", () => {
    const storage = new MemoryStorage();
    storage.setItem(readerStore.keys.progress, "{broken");
    storage.setItem(readerStore.keys.bookmarks, JSON.stringify({version:1,items:[{id:"ok",chapterId:"ch-1",anchor:null,title:"Глава",createdAt:1}]}));
    const state = readerStore.load(storage);
    expect(state.progress.items).toEqual({});
    expect(state.bookmarks.items[0]?.id).toBe("ok");
  });

  it("preserves a chapter and anchor bookmark through reload", () => {
    const storage = new MemoryStorage();
    readerStore.toggleBookmark(storage, {chapterId:"ch-1", anchor:"p-2", title:"Тест"});
    const state = readerStore.load(storage);
    expect(state.bookmarks.items).toHaveSize(1);
    expect(state.bookmarks.items[0]?.anchor).toBe("p-2");
  });

  it("stores exact reading position for restore", () => {
    const storage = new MemoryStorage();
    readerStore.saveProgress(storage, "ch-1", "p-40", 0.67);
    const state = readerStore.load(storage);
    expect(state.progress.lastPosition?.anchor).toBe("p-40");
    expect(state.progress.items["ch-1"]?.ratio).toBe(0.67);
  });
});