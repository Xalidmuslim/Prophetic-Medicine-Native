import { Link, useParams } from "react-router-dom";
import { ArrowRight } from "lucide-react";
import { PageHeader } from "../components/PageHeader";
import { useBook } from "../helpers/useBook";
import styles from "./remedies.$remedyId.module.css";

export default function RemedyPage(){
  const {remedyId}=useParams(); const {book,error,loading}=useBook();
  const remedy=book?.remedies.find((r)=>r.id===remedyId);
  const mentions=remedy?.chapterIds.map((id,index)=>({chapter:book!.chapters.find((ch)=>ch.id===id),anchor:remedy.anchors[index]})).filter((x)=>x.chapter)??[];
  return <>
    <PageHeader title={remedy?.title??"Средство"} back/>
    <div className={styles.page}>
      {loading?<p>Загрузка…</p>:error||!remedy?<p className={styles.error}>{error??"Средство не найдено"}</p>:<>
        <div className={styles.hero}><span>СРЕДСТВО ИЗ ТЕКСТА КНИГИ</span><h2>{remedy.title}</h2>{remedy.aliases.length?<p>{remedy.aliases.join(" · ")}</p>:null}</div>
        <div className={styles.notice}>Ниже показаны места книги, где встречается это средство. Приложение не добавляет дозировки или лечебные утверждения от себя.</div>
        <h3 className={styles.heading}>Упоминания в книге <span>{mentions.length}</span></h3>
        <div className={styles.list}>{mentions.map(({chapter,anchor},i)=><Link key={chapter!.id+"-"+anchor+"-"+i} to={"/read/"+chapter!.slug+(anchor?"#"+anchor:"")} className={styles.row}><span><strong>{chapter!.title}</strong><small>{chapter!.section}</small></span><ArrowRight size={17}/></Link>)}</div>
      </>}
    </div>
  </>;
}