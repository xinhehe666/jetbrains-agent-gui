import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import CommunitySection from './index';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => ({
      'settings.socialTitle': 'Agent GUI Resources',
      'settings.socialDesc': 'Documentation and support for this fork.',
      'settings.resources': 'Resources',
      'settings.socialDocs': 'Documentation',
      'settings.versionHistory': 'Version History',
      'settings.versionHistoryDesc': 'View release history',
      'settings.socialGithub': 'GitHub repository',
      'settings.socialIssues': 'Report an issue',
    })[key] ?? key,
  }),
}));

describe('CommunitySection', () => {
  it('links only to this fork repository and issue tracker', () => {
    render(<CommunitySection />);

    expect(screen.getByRole('link', { name: 'GitHub repository' }).getAttribute('href'))
      .toBe('https://github.com/xinhehe666/jetbrains-agent-gui');
    expect(screen.getByRole('link', { name: 'Report an issue' }).getAttribute('href'))
      .toBe('https://github.com/xinhehe666/jetbrains-agent-gui/issues');
    expect(screen.getByRole('link', { name: 'Documentation' }).getAttribute('href'))
      .toBe('https://github.com/xinhehe666/jetbrains-agent-gui#readme');
    expect(screen.queryByText(/WeChat|Douyin|Zhihu|YouTube/i)).toBeNull();
  });
});
