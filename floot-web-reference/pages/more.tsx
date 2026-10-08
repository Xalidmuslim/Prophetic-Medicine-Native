import { Link } from "react-router-dom";
import { ArrowRight, BookOpen, Clock3, FlaskConical, Info, Settings, ScrollText, Stethoscope, Layers3 } from "lucide-react";
import { PageHeader } from "../components/PageHeader";
import styles from "./more.module.css";

const items=[
  {to:"/book",title:"Оглавление книги",subtitle:"111 глав в оригинальном порядке",Icon:BookOpen},
  {to:"/treatments",title:"Как лечили / что применялось",subtitle:"Состояния и методы из лечебных глав",Icon:Stethoscope},
  {to:"/collections",title:"Быстрые подборки",subtitle:"Головная боль, сон, тревога, рукъя и другое",Icon:Layers3},
  {to:"/remedies",title:"Справочник средств",subtitle:"Переходы к местам в полном тексте",Icon:FlaskConical},
  {to:"/history",title:"История чтения",subtitle:"Недавно открытые главы",Icon:Clock3},
  {to:"/settings",title:"Настройки чтения",subtitle:"Шрифт, интервал и оформление",Icon:Settings},
  {to:"/about",title:"О приложении",subtitle:"Назначение и границы использования",Icon:Info},
  {to:"/source",title:"Об источнике текста",subtitle:"Структура и полнота издания",Icon:ScrollText},
];
export default function MorePage(){return <><PageHeader title="Ещё"/><div className={styles.page}><div className={styles.list}>{items.map(({to,title,subtitle,Icon})=><Link key={to} to={to} className={styles.row}><span className={styles.icon}><Icon size={19}/></span><span><strong>{title}</strong><small>{subtitle}</small></span><ArrowRight size={16}/></Link>)}</div></div></>}