import { enUS } from './en-US';
import { esES } from './es-ES';
import type { Language } from '../../contexts/LanguageContext';

export const translations = {
  'en-US': enUS,
  'es-ES': esES,
};

export const getTranslations = (language: Language) => {
  return translations[language];
};
