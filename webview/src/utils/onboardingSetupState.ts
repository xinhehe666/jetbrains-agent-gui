export const ONBOARDING_STEP_IDS = ['node', 'claude-cli', 'sdk'] as const;

export type OnboardingStepId = typeof ONBOARDING_STEP_IDS[number];
export type OnboardingSetupStatus = 'ready' | 'actionRequired' | 'checking' | 'complete';
export type OnboardingStatus = OnboardingSetupStatus;
export type OnboardingStepStatus = 'actionRequired' | 'complete' | 'optional' | 'checking';

export interface OnboardingSetupInput {
  node?: unknown;
  nodeEnvironment?: unknown;
  nodeAvailable?: unknown;
  claudeCli?: unknown;
  'claude-cli'?: unknown;
  cli?: unknown;
  claude?: unknown;
  claudeCliInstalled?: unknown;
  sdk?: unknown;
  sdkStatus?: unknown;
  dependencies?: unknown;
  [key: string]: unknown;
}

export interface OnboardingStepState {
  status: OnboardingStepStatus;
  required: boolean;
}

export interface OnboardingSetupState {
  status: OnboardingSetupStatus;
  ready: boolean;
  actionRequired: boolean;
  complete: boolean;
  firstActionableStep: OnboardingStepId | null;
  steps: Readonly<Record<OnboardingStepId, OnboardingStepState>>;
}

const COMPLETE_STATUS_VALUES = new Set(['complete', 'installed', 'available', 'configured']);
const ACTION_REQUIRED_STATUS_VALUES = new Set([
  'actionrequired',
  'error',
  'failed',
  'missing',
  'notinstalled',
  'unavailable',
]);
const OPTIONAL_STATUS_VALUES = new Set(['optional', 'skipped', 'notrequired']);
const CHECKING_STATUS_VALUES = new Set([
  'checking',
  'pending',
  'loading',
  'unknown',
  'inprogress',
]);

function isRecord(value: unknown): value is Record<string, unknown> {
  return value !== null && typeof value === 'object' && !Array.isArray(value);
}

function normalizeStatus(value: unknown): OnboardingStepStatus | undefined {
  if (typeof value === 'boolean') {
    return value ? 'complete' : 'actionRequired';
  }
  if (typeof value !== 'string') {
    return undefined;
  }

  const normalized = value.trim().toLowerCase().replaceAll(/[-_\s]/g, '');
  if (COMPLETE_STATUS_VALUES.has(normalized)) {
    return 'complete';
  }
  if (ACTION_REQUIRED_STATUS_VALUES.has(normalized)) {
    return 'actionRequired';
  }
  if (OPTIONAL_STATUS_VALUES.has(normalized)) {
    return 'optional';
  }
  if (CHECKING_STATUS_VALUES.has(normalized)) {
    return 'checking';
  }
  return undefined;
}

function readStatus(value: unknown, booleanKeys: readonly string[]): OnboardingStepStatus | undefined {
  const directStatus = normalizeStatus(value);
  if (directStatus !== undefined) {
    return directStatus;
  }
  if (!isRecord(value)) {
    return undefined;
  }

  for (const key of booleanKeys) {
    const status = normalizeStatus(value[key]);
    if (status !== undefined) {
      return status;
    }
  }
  for (const key of ['status', 'state', 'result']) {
    const status = normalizeStatus(value[key]);
    if (status !== undefined) {
      return status;
    }
  }
  return undefined;
}

function readNestedStatus(
  value: unknown,
  nestedKeys: readonly string[],
  booleanKeys: readonly string[],
  fallback: OnboardingStepStatus,
): OnboardingStepStatus {
  const directStatus = readStatus(value, booleanKeys);
  if (directStatus !== undefined) {
    return directStatus;
  }
  if (isRecord(value)) {
    for (const key of nestedKeys) {
      const nestedStatus = readStatus(value[key], booleanKeys);
      if (nestedStatus !== undefined) {
        return nestedStatus;
      }
    }
  }
  return fallback;
}

const STEP_STATUS_READERS: Readonly<Record<OnboardingStepId, (input: OnboardingSetupInput) => OnboardingStepStatus>> = {
  node: (input) => readNestedStatus(
    input.node ?? input.nodeEnvironment ?? input.nodeAvailable,
    ['node', 'nodeEnvironment'],
    ['available', 'installed', 'configured'],
    'actionRequired',
  ),
  'claude-cli': (input) => readNestedStatus(
    input.claudeCli ?? input['claude-cli'] ?? input.cli ?? input.claude ?? input.claudeCliInstalled,
    ['claude-cli', 'claudeCli', 'claude', 'cli'],
    ['available', 'installed', 'configured'],
    'optional',
  ),
  sdk: (input) => {
    const sdkInput = input.sdk ?? input.sdkStatus ?? input.dependencies;
    return readNestedStatus(
      sdkInput ?? input,
      ['sdk', 'sdkStatus', 'dependencies', 'claude-sdk'],
      ['available', 'installed', 'configured'],
      'actionRequired',
    );
  },
};

const STEP_REQUIRED: Readonly<Record<OnboardingStepId, boolean>> = {
  node: true,
  'claude-cli': false,
  sdk: true,
};

export function deriveOnboardingSetupState(input: unknown): OnboardingSetupState {
  const source = isRecord(input) ? input : {};
  const steps = Object.fromEntries(
    ONBOARDING_STEP_IDS.map((stepId) => [
      stepId,
      {
        status: STEP_STATUS_READERS[stepId](source),
        required: STEP_REQUIRED[stepId],
      },
    ]),
  ) as Record<OnboardingStepId, OnboardingStepState>;
  const firstActionableStep = ONBOARDING_STEP_IDS.find((stepId) => steps[stepId].status === 'actionRequired') ?? null;
  const actionRequired = firstActionableStep !== null;
  const checking = ONBOARDING_STEP_IDS.some(
    (stepId) => steps[stepId].required && steps[stepId].status === 'checking',
  );
  const complete = !actionRequired && ONBOARDING_STEP_IDS.every(
    (stepId) => !steps[stepId].required || steps[stepId].status === 'complete',
  );
  const ready = !actionRequired && !checking;
  const status: OnboardingSetupStatus = complete
    ? 'complete'
    : actionRequired
      ? 'actionRequired'
      : checking
        ? 'checking'
        : 'ready';

  return {
    status,
    ready,
    actionRequired,
    complete,
    firstActionableStep,
    steps,
  };
}

export const getOnboardingSetupState = deriveOnboardingSetupState;
export const createOnboardingSetupState = deriveOnboardingSetupState;
