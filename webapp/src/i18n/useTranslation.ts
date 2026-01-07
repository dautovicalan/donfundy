import { useLanguage } from '../contexts/LanguageContext';
import { getTranslations } from './translations';

export const useTranslation = () => {
  const { language } = useLanguage();
  const t = getTranslations(language);

  return { t, language };
};
