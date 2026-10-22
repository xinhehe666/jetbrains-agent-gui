import { useCallback, useState } from 'react';
import type { MouseEvent } from 'react';
import { useTranslation } from 'react-i18next';
import ChangelogDialog from '../../ChangelogDialog';
import { CHANGELOG_DATA } from '../../../version/changelog';
import { openBrowser } from '../../../utils/bridge';
import styles from './style.module.less';

const GITHUB_URL = 'https://github.com/xinhehe666/jetbrains-cc-gui';
const ISSUES_URL = `${GITHUB_URL}/issues`;

const GITHUB_PATH =
  'M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12';

const BrandIcon = ({ path }: { path: string }) => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
    <path d={path} />
  </svg>
);

const CommunitySection = () => {
  const { t } = useTranslation();
  const [showChangelog, setShowChangelog] = useState(false);

  // JCEF won't route target=_blank to the system browser — go through the bridge.
  const openExternal = useCallback((event: MouseEvent<HTMLAnchorElement>, url: string) => {
    event.preventDefault();
    openBrowser(url);
  }, []);

  return (
    <div className={styles.configSection}>
      <h3 className={styles.sectionTitle}>{t('settings.socialTitle')}</h3>
      <p className={styles.sectionDesc}>{t('settings.socialDesc')}</p>
      <div className={styles.socialSection}>
        <div className={styles.socialHeader}>
          <span className={styles.resourceLabel}>{t('settings.resources')}</span>
          <div className={styles.socialHeaderActions}>
            <a
              className={styles.versionHistoryBtn}
              href={`${GITHUB_URL}#readme`}
              target="_blank"
              rel="noreferrer noopener"
              onClick={(e) => openExternal(e, `${GITHUB_URL}#readme`)}
              title={t('settings.socialDocs')}
            >
              <span className="codicon codicon-book" />
              {t('settings.socialDocs')}
            </a>
            <button
              type="button"
              className={styles.versionHistoryBtn}
              onClick={() => setShowChangelog(true)}
              title={t('settings.versionHistoryDesc')}
            >
              <span className="codicon codicon-history" />
              {t('settings.versionHistory')}
            </button>
          </div>
        </div>

        <div className={styles.socialRow}>
          <a
            className={styles.socialChip}
            href={GITHUB_URL}
            target="_blank"
            rel="noreferrer noopener"
            onClick={(e) => openExternal(e, GITHUB_URL)}
            title={t('settings.socialGithub')}
            aria-label={t('settings.socialGithub')}
          >
            <BrandIcon path={GITHUB_PATH} />
          </a>
          <a
            className={styles.socialChip}
            href={ISSUES_URL}
            target="_blank"
            rel="noreferrer noopener"
            onClick={(e) => openExternal(e, ISSUES_URL)}
            title={t('settings.socialIssues')}
            aria-label={t('settings.socialIssues')}
          >
            <span className="codicon codicon-issues" />
          </a>
        </div>
      </div>

      <ChangelogDialog
        isOpen={showChangelog}
        onClose={() => setShowChangelog(false)}
        entries={CHANGELOG_DATA}
      />
    </div>
  );
};

export default CommunitySection;
