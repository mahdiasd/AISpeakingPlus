export interface CefrLevel {
  code: string;
  titleFa: string;
  titleEn: string;
  descriptionFa: string;
  badgeBg: string;
  badgeBorder: string;
  badgeText: string;
}

export const CEFR_LEVELS: CefrLevel[] = [
  {
    code: 'A1',
    titleFa: 'مبتدی',
    titleEn: 'Beginner',
    descriptionFa: 'آشنایی با کلمات ساده، معرفی خود و اصطلاحات روزمره',
    badgeBg: 'bg-apple-blue/15',
    badgeBorder: 'border-apple-blue/30',
    badgeText: 'text-apple-blue',
  },
  {
    code: 'A2',
    titleFa: 'پیش‌متوسط',
    titleEn: 'Elementary',
    descriptionFa: 'درک عبارات پرکاربرد و مکالمات ساده پیرامون کارهای روزانه',
    badgeBg: 'bg-sky-500/15',
    badgeBorder: 'border-sky-500/30',
    badgeText: 'text-sky-400',
  },
  {
    code: 'B1',
    titleFa: 'متوسط',
    titleEn: 'Intermediate',
    descriptionFa: 'مکالمه در موقعیت‌های سفر، کار و بیان تجربیات و اهداف',
    badgeBg: 'bg-purple-500/15',
    badgeBorder: 'border-purple-500/30',
    badgeText: 'text-purple-400',
  },
  {
    code: 'B2',
    titleFa: 'فوق‌متوسط',
    titleEn: 'Upper-Intermediate',
    descriptionFa: 'صحبت روان و موثر در بحث‌های فنی، اجتماعی و تخصصی',
    badgeBg: 'bg-indigo-500/15',
    badgeBorder: 'border-indigo-500/30',
    badgeText: 'text-indigo-400',
  },
  {
    code: 'C1',
    titleFa: 'پیشرفته',
    titleEn: 'Advanced',
    descriptionFa: 'درک آسان مفاهیم پیچیده و کاربرد منعطف و خلاقانه زبان',
    badgeBg: 'bg-apple-yellow/15',
    badgeBorder: 'border-apple-yellow/30',
    badgeText: 'text-apple-yellow',
  },
  {
    code: 'C2',
    titleFa: 'مسلط / بومی',
    titleEn: 'Mastery',
    descriptionFa: 'تسلط کامل، دقیق، سریع و بی‌وقفه در تمامی ابعاد زبان',
    badgeBg: 'bg-amber-500/15',
    badgeBorder: 'border-amber-500/30',
    badgeText: 'text-amber-400',
  },
];

export function getCefrInfo(code?: string | null): CefrLevel {
  const normalized = (code || 'A1').trim().toUpperCase();
  return CEFR_LEVELS.find((lvl) => lvl.code === normalized) || CEFR_LEVELS[0];
}
