import { registerPlugin } from '@capacitor/core';

import type { AppboxoPlugin } from './definitions';

const Appboxo = registerPlugin<AppboxoPlugin>('Appboxo');

export * from './definitions';
export { Appboxo };
