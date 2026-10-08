export const readerStore = (() => {
  const keys = {
    progress:"mp:v1:progress",
    bookmarks:"mp:v1:bookmarks",
    history:"mp:v1:history",
    settings:"mp:v1:settings",
    favoriteTopics:"mp:v1:favorite-topics",
  } as const;

  const defaults = {
    progress:{version:1,items:{} as Record<string,{anchor:string|null;ratio:number;updatedAt:number}>,lastPosition:null as null|{chapterId:string;anchor:string|null},readChapters:[] as string[]},
    bookmarks:{version:1,items:[] as Array<{id:string;chapterId:string;anchor:string|null;title:string;createdAt:number}>},
    history:{version:1,items:[] as Array<{chapterId:string;title:string;anchor:string|null;visitedAt:number}>},
    settings:{version:1,theme:"system" as "system"|"light"|"dark",fontSize:"medium" as "small"|"medium"|"large"|"xlarge",lineHeight:"standard" as "compact"|"standard"|"relaxed",showHistoricalLabels:true},
    favoriteTopics:{version:1,items:[] as string[]},
  };

  type StorageLike = Pick<Storage,"getItem"|"setItem"|"removeItem">;
  const clone = <T,>(value:T):T => JSON.parse(JSON.stringify(value));
  const safe = <T extends {version:number}>(storage:StorageLike,key:string,fallback:T):T => {
    try {
      const raw = storage.getItem(key);
      if (!raw) return clone(fallback);
      const parsed = JSON.parse(raw);
      if (!parsed || typeof parsed !== "object" || parsed.version !== fallback.version) {
        storage.removeItem(key);
        return clone(fallback);
      }
      return parsed as T;
    } catch {
      try { storage.removeItem(key); } catch { /* storage may be unavailable */ }
      return clone(fallback);
    }
  };
  const save = (storage:StorageLike,key:string,value:unknown) => storage.setItem(key,JSON.stringify(value));

  const load = (storage:StorageLike = window.localStorage) => ({
    progress:safe(storage,keys.progress,defaults.progress),
    bookmarks:safe(storage,keys.bookmarks,defaults.bookmarks),
    history:safe(storage,keys.history,defaults.history),
    settings:safe(storage,keys.settings,defaults.settings),
    favoriteTopics:safe(storage,keys.favoriteTopics,defaults.favoriteTopics),
  });

  const saveProgress = (storage:StorageLike,chapterId:string,anchor:string|null,ratio:number) => {
    const state = safe(storage,keys.progress,defaults.progress);
    const bounded = Math.max(0,Math.min(1,ratio));
    state.items[chapterId] = {anchor,ratio:bounded,updatedAt:Date.now()};
    state.lastPosition = {chapterId,anchor};
    if (bounded >= .96 && !state.readChapters.includes(chapterId)) state.readChapters.push(chapterId);
    save(storage,keys.progress,state);
  };

  const toggleBookmark = (storage:StorageLike,input:{chapterId:string;anchor:string|null;title:string}) => {
    const state = safe(storage,keys.bookmarks,defaults.bookmarks);
    const id = `${input.chapterId}::${input.anchor ?? "chapter"}`;
    const found = state.items.findIndex((item) => item.id === id);
    if (found >= 0) state.items.splice(found,1);
    else state.items.unshift({id,...input,createdAt:Date.now()});
    save(storage,keys.bookmarks,state);
    return found < 0;
  };

  const addHistory = (storage:StorageLike,entry:{chapterId:string;title:string;anchor:string|null}) => {
    const state = safe(storage,keys.history,defaults.history);
    state.items = [
      {...entry,visitedAt:Date.now()},
      ...state.items.filter((item) => item.chapterId !== entry.chapterId),
    ].slice(0,30);
    save(storage,keys.history,state);
  };

  const saveSettings = (storage:StorageLike,patch:Partial<typeof defaults.settings>) => {
    const state = safe(storage,keys.settings,defaults.settings);
    const next = {...state,...patch,version:1};
    save(storage,keys.settings,next);
    return next;
  };

  const toggleFavoriteTopic = (storage:StorageLike,topicId:string) => {
    const state = safe(storage,keys.favoriteTopics,defaults.favoriteTopics);
    state.items = state.items.includes(topicId) ? state.items.filter((id) => id !== topicId) : [topicId,...state.items];
    save(storage,keys.favoriteTopics,state);
    return state.items;
  };

  const resetProgress = (storage:StorageLike) => {
    storage.removeItem(keys.progress);
    storage.removeItem(keys.history);
  };

  return {keys,defaults,load,saveProgress,toggleBookmark,addHistory,saveSettings,toggleFavoriteTopic,resetProgress};
})();