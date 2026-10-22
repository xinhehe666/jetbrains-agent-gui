import { useTranslation } from 'react-i18next';
import { openBrowser } from '../../../utils/bridge';
import {
  ARROW_CONTAINER_STYLE,
  ARROW_ICON_STYLE,
  ITEM_INFO_STYLE,
} from './selectorStyles';

const DOCS_URLS: Record<string, string> = {
  zh: 'https://github.com/xinhehe666/jetbrains-agent-gui#readme',
  'zh-TW': 'https://github.com/xinhehe666/jetbrains-agent-gui#readme',
};
const DEFAULT_DOCS_URL = 'https://github.com/xinhehe666/jetbrains-agent-gui#readme';

const resolveDocsUrl = (language: string): string => {
  if (language.startsWith('zh-TW') || language.startsWith('zh-Hant')) {
    return DOCS_URLS['zh-TW'];
  }
  if (language.startsWith('zh')) {
    return DOCS_URLS.zh;
  }
  return DEFAULT_DOCS_URL;
};

interface OfficialDocsOptionProps {
  onClose: () => void;
  onMouseEnter: () => void;
}

/**
 * OfficialDocsOption - Footer row of the ConfigSelect dropdown linking to the
 * documentation for this fork.
 */
export const OfficialDocsOption = ({ onClose, onMouseEnter }: OfficialDocsOptionProps) => {
  const { t, i18n } = useTranslation();

  return (
    <div
      className="selector-option"
      data-testid="config-option-official-docs"
      role="button"
      tabIndex={0}
      onClick={(e) => {
        e.stopPropagation();
        openBrowser(resolveDocsUrl(i18n.language));
        onClose();
      }}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          e.stopPropagation();
          openBrowser(resolveDocsUrl(i18n.language));
          onClose();
        }
      }}
      onMouseEnter={onMouseEnter}
    >
      <span className="codicon codicon-book" />
      <div style={ITEM_INFO_STYLE}>
        <span>{t('config.officialDocs')}</span>
      </div>
      <div style={ARROW_CONTAINER_STYLE}>
        <span className="codicon codicon-link-external" style={ARROW_ICON_STYLE} />
      </div>
    </div>
  );
};

export default OfficialDocsOption;
