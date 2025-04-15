import { registerPlugin } from '@capacitor/core';

import type { AppboxoPlugin } from './definitions';

const Boxo = registerPlugin<AppboxoPlugin>('Appboxo');
const Appboxo = registerPlugin<AppboxoPlugin>('Appboxo');

export * from './definitions';
export { Appboxo };
export { Boxo };
