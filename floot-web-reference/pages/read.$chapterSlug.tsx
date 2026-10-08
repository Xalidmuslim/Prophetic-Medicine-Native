import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Bookmark, BookOpen, Check, ChevronLeft, ChevronRight, Copy, Settings } from "lucide-react";
import { Button } from "../components/Button";
import { ContentBlockView } from "../components/ContentBlockView";
import { useBook } from "../helpers/useBook";
import { readerStore } from "../helpers/readerStore";
import { BookModel } from "../helpers/BookModel";
import { CopyTools } from "../helpers/CopyTools";
import styles from "./read.$chapterSlug.module.css";

export default function ReaderPage(){
  const {chapterSlug}=useParams();
  const navigate=useNavigate();
  const {book,error,loading}=useBook();
  const chapter=book?.chapters.find((ch)=>ch.slug===chapterSlug);
  const [state,setState]=useState(()=>typeof window==="undefined"?readerStore.load({getItem:()=>null,setItem:()=>{},removeItem:()=>{}} as unknown as Storage):readerStore.load(window.localStorage));
  const [chapterCopied,setChapterCopied]=useState(false);
  const container=useRef<HTMLDivElement>(null);
  const previous=chapter?.previousChapterId?book?.chapters.find((ch)=>ch.id===chapter.previousChapterId):null;
  const next=chapter?.nextChapterId?book?.chapters.find((ch)=>ch.id===chapter.nextChapterId):null;
  const settings=state.settings;

  useEffect(()=>{
    if(!chapter || typeof window==="undefined") return;
    readerStore.addHistory(window.localStorage,{chapterId:chapter.id,title:chapter.title,anchor:state.progress.items[chapter.id]?.anchor??null});
    const target=(window.location.hash.slice(1) || state.progress.items[chapter.id]?.anchor);
    const timer=window.setTimeout(()=>{ if(target) document.getElementById(target)?.scrollIntoView({block:"start"}); else window.scrollTo({top:0}); },60);
    let ticking=false;
    const save=()=>{
      ticking=false;
      const nodes=[...(container.current?.querySelectorAll<HTMLElement>("[data-anchor]")??[])];
      let anchor:string|null=null;
      for(const node of nodes){ if(node.getBoundingClientRect().top<=115) anchor=node.dataset.anchor??anchor; else break; }
      const max=Math.max(1,document.documentElement.scrollHeight-window.innerHeight);
      readerStore.saveProgress(window.localStorage,chapter.id,anchor,window.scrollY/max);
    };
    const onScroll=()=>{if(!ticking){ticking=true;requestAnimationFrame(save);}};
    window.addEventListener("scroll",onScroll,{passive:true});
    return()=>{clearTimeout(timer);window.removeEventListener("scroll",onScroll);save();};
  },[chapter?.id]);

  const vars=useMemo(()=>({
    "--reader-font-size":settings.fontSize==="small"?"1rem":settings.fontSize==="large"?"1.18rem":settings.fontSize==="xlarge"?"1.3rem":"1.08rem",
    "--reader-line-height":settings.lineHeight==="compact"?"1.55":settings.lineHeight==="relaxed"?"1.95":"1.78",
  } as React.CSSProperties),[settings.fontSize,settings.lineHeight]);

  const chapterMarked=chapter?state.bookmarks.items.some((item)=>item.chapterId===chapter.id&&item.anchor===null):false;
  const toggleChapter=()=>{if(!chapter)return;readerStore.toggleBookmark(window.localStorage,{chapterId:chapter.id,anchor:null,title:chapter.title});setState(readerStore.load(window.localStorage));};
  const toggleBlock=(block:BookModel.ContentBlock)=>{if(!chapter)return;readerStore.toggleBookmark(window.localStorage,{chapterId:chapter.id,anchor:block.anchor,title:`${chapter.title} — ${block.text.slice(0,48)}`});setState(readerStore.load(window.localStorage));};
  const copyChapter=async()=>{if(!chapter)return;const ok=await CopyTools.copy(CopyTools.chapterText(chapter));if(!ok)return;setChapterCopied(true);window.setTimeout(()=>setChapterCopied(false),1600);};

  if(loading)return <div className={styles.status}>Загружаем главу…</div>;
  if(error||!chapter)return <div className={styles.status}>{error??"Глава не найдена"}<Button onClick={()=>navigate("/book")}>К оглавлению</Button></div>;
  return <div className={styles.reader} style={vars}>
    <header className={styles.header}>
      <div className={styles.headerInner}>
        <Button variant="ghost" size="icon-md" aria-label="Назад" onClick={()=>navigate(-1)}><ArrowLeft size={20}/></Button>
        <div className={styles.headerTitle}><strong>{chapter.title}</strong><span>Глава {chapter.order} из {book!.chapters.length}</span></div>
        <div className={styles.headerActions}>
          <Button variant={chapterMarked?"secondary":"ghost"} size="icon-sm" aria-label="Закладка главы" onClick={toggleChapter}><Bookmark size={18}/></Button>
          <Button asChild variant="ghost" size="icon-sm" aria-label="Настройки чтения"><Link to="/settings"><Settings size={18}/></Link></Button>
        </div>
      </div>
    </header>
    <article className={styles.article} ref={container}>
      <div className={styles.meta}><Link to="/book"><BookOpen size={14}/> Содержание</Link><span>{chapter.section}</span></div>
      <h1>{chapter.title}</h1>
      <div className={styles.titleTools}><Button variant="outline" size="sm" onClick={copyChapter}>{chapterCopied?<Check size={16}/>:<Copy size={16}/>} {chapterCopied?"Скопировано":"Скопировать главу"}</Button></div>
      <div className={styles.rule}/>
      {chapter.contentBlocks.map((block)=><ContentBlockView key={block.id} block={block} showHistoricalLabels={settings.showHistoricalLabels} onBookmark={toggleBlock}/>)}
      <nav className={styles.chapterNav}>
        {previous?<Link to={`/read/${previous.slug}`} className={styles.prev}><ChevronLeft size={18}/><span><small>Предыдущая</small>{previous.title}</span></Link>:<span/>}
        {next?<Link to={`/read/${next.slug}`} className={styles.next}><span><small>Следующая</small>{next.title}</span><ChevronRight size={18}/></Link>:<span/>}
      </nav>
    </article>
  </div>;
}