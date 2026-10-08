import { Link } from "react-router-dom";
import { ArrowRight, BookOpen, Bookmark, FlaskConical, LayoutGrid, Search, Stethoscope } from "lucide-react";
import { Button } from "../components/Button";
import { PageHeader } from "../components/PageHeader";
import { useBook } from "../helpers/useBook";
import { readerStore } from "../helpers/readerStore";
import { buildQuickCollections } from "../helpers/buildQuickCollections";
import styles from "./_index.module.css";

export default function HomePage(){
  const {book,error,loading}=useBook();
  const state = typeof window === "undefined" ? readerStore.defaults : readerStore.load(window.localStorage);
  const last = book && state.progress.lastPosition ? book.chapters.find((ch)=>ch.id===state.progress.lastPosition?.chapterId) : null;
  const readCount = state.progress.readChapters.length;
  const progress = book?.chapters.length ? Math.round(readCount/book.chapters.length*100) : 0;
  const recent = book ? state.history.items.slice(0,3).map((item)=>book.chapters.find((ch)=>ch.id===item.chapterId)).filter(Boolean) : [];
  const favoriteTopics = book?.topics.filter((topic)=>state.favoriteTopics.items.includes(topic.id)).slice(0,4) ?? [];
  const quickCollections = book ? buildQuickCollections(book).slice(0,4) : [];

  return <>
    <PageHeader title="Медицина Пророка ﷺ" subtitle="Ибн Каййим аль-Джаузия"/>
    <div className={styles.page}>
      <section className={styles.intro}>
        <span className={styles.eyebrow}>ПОЛНЫЙ ТЕКСТ · ЧТЕНИЕ И ИЗУЧЕНИЕ</span>
        <h2>Книга, разбитая на главы, темы и средства</h2>
        <p>Полный русский текст без сокращения лечебных описаний автора.</p>
      </section>

      {loading ? <div className={styles.card}>Загружаем книгу…</div> : error ? <div className={styles.error}>{error}</div> : (
        <>
          <section className={styles.continueCard}>
            <div className={styles.cardTop}>
              <div><span className={styles.label}>Продолжить чтение</span><h3>{last?.title ?? "Начать с первой главы"}</h3></div>
              <span className={styles.percent}>{progress}%</span>
            </div>
            <div className={styles.progress}><span style={{width:`${progress}%`}}/></div>
            <Button asChild size="lg"><Link to={last ? `/read/${last.slug}${state.progress.lastPosition?.anchor ? `#${state.progress.lastPosition.anchor}` : ""}` : `/read/${book!.chapters[0]?.slug}`}>{last ? "Продолжить" : "Начать чтение"}<ArrowRight size={18}/></Link></Button>
          </section>

          <Link to="/search" className={styles.searchShortcut}><Search size={19}/><span>Поиск по всей книге</span></Link>
          <Link to="/treatments" className={styles.treatmentShortcut}>
            <span className={styles.treatmentIcon}><Stethoscope size={20}/></span>
            <span><strong>Как лечили / что применялось</strong><small>Состояния, средства и переход в полный текст</small></span>
            <ArrowRight size={18}/>
          </Link>

          <section className={styles.quickGrid}>
            <Quick to="/book" icon={<BookOpen/>} title="Читать книгу" meta={`${book!.chapters.length} глав`}/>
            <Quick to="/topics" icon={<LayoutGrid/>} title="Темы" meta={`${book!.topics.length} разделов`}/>
            <Quick to="/remedies" icon={<FlaskConical/>} title="Средства" meta={`${book!.remedies.length} позиций`}/>
            <Quick to="/bookmarks" icon={<Bookmark/>} title="Закладки" meta={`${state.bookmarks.items.length} сохранено`}/>
          </section>

          <section className={styles.section}>
            <div className={styles.sectionTitle}><h3>Быстрые подборки</h3><Link to="/collections">Все</Link></div>
            <div className={styles.collectionLinks}>{quickCollections.map((item)=><Link key={item.id} to={"/collections/"+item.id}><strong>{item.title}</strong><small>{item.chapterIds.length} глав</small></Link>)}</div>
          </section>

          {recent.length ? <section className={styles.section}><div className={styles.sectionTitle}><h3>Недавно читали</h3><Link to="/history">История</Link></div>{recent.map((ch)=><Link key={ch!.id} to={`/read/${ch!.slug}`} className={styles.row}><span>{ch!.title}</span><ArrowRight size={16}/></Link>)}</section> : null}
          {favoriteTopics.length ? <section className={styles.section}><div className={styles.sectionTitle}><h3>Избранные темы</h3></div><div className={styles.chips}>{favoriteTopics.map((topic)=><Link key={topic.id} to={`/topics/${topic.id}`}>{topic.title}</Link>)}</div></section> : null}
        </>
      )}
    </div>
  </>;
}

function Quick({to,icon,title,meta}:{to:string;icon:React.ReactNode;title:string;meta:string}){
  return <Link to={to} className={styles.quick}><span className={styles.quickIcon}>{icon}</span><strong>{title}</strong><small>{meta}</small></Link>;
}