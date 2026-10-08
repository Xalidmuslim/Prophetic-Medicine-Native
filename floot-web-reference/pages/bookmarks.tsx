import { useState } from "react";
import { Link } from "react-router-dom";
import { Bookmark, Trash2 } from "lucide-react";
import { Button } from "../components/Button";
import { PageHeader } from "../components/PageHeader";
import { useBook } from "../helpers/useBook";
import { readerStore } from "../helpers/readerStore";
import styles from "./bookmarks.module.css";

export default function BookmarksPage(){
  const {book,error,loading}=useBook();
  const [state,setState]=useState(()=>typeof window==="undefined"?readerStore.defaults.bookmarks:readerStore.load(window.localStorage).bookmarks);
  const remove=(chapterId:string,anchor:string|null,title:string)=>{readerStore.toggleBookmark(window.localStorage,{chapterId,anchor,title});setState(readerStore.load(window.localStorage).bookmarks);};
  return <>
    <PageHeader title="Закладки" subtitle={state.items.length ? state.items.length+" сохранено" : undefined}/>
    <div className={styles.page}>
      {loading?<p>Загрузка…</p>:error?<p className={styles.error}>{error}</p>:state.items.length?<div className={styles.list}>{state.items.map((item)=>{
        const chapter=book!.chapters.find((ch)=>ch.id===item.chapterId); if(!chapter)return null;
        return <div key={item.id} className={styles.row}><Link to={"/read/"+chapter.slug+(item.anchor?"#"+item.anchor:"")}><span className={styles.kind}>{item.anchor?"Место в тексте":"Глава"}</span><strong>{item.title}</strong><small>{chapter.section}</small></Link><Button variant="ghost" size="icon-sm" aria-label="Удалить закладку" onClick={()=>remove(item.chapterId,item.anchor,item.title)}><Trash2 size={16}/></Button></div>;
      })}</div>:<div className={styles.empty}><Bookmark size={30}/><h2>Закладок пока нет</h2><p>В ридере нажмите на значок закладки у главы или конкретного абзаца.</p><Button asChild variant="outline"><Link to="/book">Открыть книгу</Link></Button></div>}
    </div>
  </>;
}