import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { ArrowRight, BookOpen, Search, ShieldCheck, Sparkles, Stethoscope } from "lucide-react";
import { Input } from "../components/Input";
import { Button } from "../components/Button";
import { PageHeader } from "../components/PageHeader";
import { useBook } from "../helpers/useBook";
import { buildTreatmentIndex, TreatmentIndexItem } from "../helpers/buildTreatmentIndex";
import styles from "./treatments.module.css";

const categories:Array<"Все"|TreatmentIndexItem["category"]>=["Все","Телесные состояния","Рукъя и защита","Состояния сердца","Предосторожность и поддержка"];
const norm=(v:string)=>v.toLocaleLowerCase("ru").replaceAll("ё","е");

export default function TreatmentsPage(){
  const {book,error,loading}=useBook();
  const [query,setQuery]=useState("");
  const [category,setCategory]=useState<(typeof categories)[number]>("Все");
  const all=useMemo(()=>book?buildTreatmentIndex(book):[],[book]);
  const items=useMemo(()=>all.filter((item)=>{
    if(category!=="Все"&&item.category!==category)return false;
    if(!query.trim())return true;
    const hay=norm([item.condition,item.chapterTitle,...item.methods,...item.remedies.map((r)=>r.title)].join(" "));
    return hay.includes(norm(query.trim()));
  }),[all,query,category]);

  return <>
    <PageHeader title="Как лечили" subtitle="Что применялось в тексте книги" back/>
    <div className={styles.page}>
      <section className={styles.intro}>
        <span>НАВИГАЦИЯ ПО ЛЕЧЕБНЫМ ГЛАВАМ</span>
        <h2>Состояние → что упоминается → полный текст</h2>
        <p>Раздел показывает содержание книги Ибн аль-Каййима. Это историко-религиозный справочник, а не индивидуальная медицинская инструкция.</p>
      </section>

      <div className={styles.search}><Search size={18}/><Input value={query} onChange={(e)=>setQuery(e.target.value)} placeholder="Например: головная боль, сглаз, хиджама…"/></div>
      <div className={styles.filters}>{categories.map((item)=><Button key={item} variant={category===item?"primary":"outline"} size="sm" onClick={()=>setCategory(item)}>{item}</Button>)}</div>

      {loading?<p className={styles.status}>Загружаем лечебные главы…</p>:error?<p className={styles.error}>{error}</p>:items.length?
        <div className={styles.list}>{items.map((item)=><TreatmentCard key={item.id} item={item}/>)}</div>
        :<div className={styles.empty}>По этому запросу ничего не найдено.</div>}
    </div>
  </>;
}

function TreatmentCard({item}:{item:TreatmentIndexItem}){
  const Icon=item.category==="Рукъя и защита"?ShieldCheck:item.category==="Состояния сердца"?Sparkles:Stethoscope;
  const chips=[...item.methods,...item.remedies.map((r)=>r.title)].filter((value,index,array)=>array.indexOf(value)===index).slice(0,7);
  return <article className={styles.card}>
    <div className={styles.cardHead}><span className={styles.icon}><Icon size={18}/></span><span className={styles.category}>{item.category}</span></div>
    <h3>{item.condition}</h3>
    {chips.length?<div className={styles.chips}>{chips.map((chip)=><span key={chip}>{chip}</span>)}</div>:<p className={styles.noChips}>В карточке не выделено отдельное средство — подробности находятся в полном тексте главы.</p>}
    <Link to={"/read/"+item.chapterSlug} className={styles.open}><BookOpen size={16}/><span>Открыть полную главу</span><ArrowRight size={16}/></Link>
  </article>;
}