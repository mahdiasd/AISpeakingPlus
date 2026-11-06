export interface ProfileAvatarOption {
  id: string; // e.g. "avatar_g1"
  label: string; // e.g. "دختر ۱" or "G-1"
  category: 'girls' | 'boys';
  imageSrc: string;
}

export const PROFILE_AVATARS: ProfileAvatarOption[] = [
  // Girls 1-12
  { id: 'avatar_g1', label: 'دختر ۱', category: 'girls', imageSrc: '/resources/avatar_g1.png' },
  { id: 'avatar_g2', label: 'دختر ۲', category: 'girls', imageSrc: '/resources/avatar_g2.png' },
  { id: 'avatar_g3', label: 'دختر ۳', category: 'girls', imageSrc: '/resources/avatar_g3.png' },
  { id: 'avatar_g4', label: 'دختر ۴', category: 'girls', imageSrc: '/resources/avatar_g4.png' },
  { id: 'avatar_g5', label: 'دختر ۵', category: 'girls', imageSrc: '/resources/avatar_g5.png' },
  { id: 'avatar_g6', label: 'دختر ۶', category: 'girls', imageSrc: '/resources/avatar_g6.png' },
  { id: 'avatar_g7', label: 'دختر ۷', category: 'girls', imageSrc: '/resources/avatar_g7.png' },
  { id: 'avatar_g8', label: 'دختر ۸', category: 'girls', imageSrc: '/resources/avatar_g8.png' },
  { id: 'avatar_g9', label: 'دختر ۹', category: 'girls', imageSrc: '/resources/avatar_g9.png' },
  { id: 'avatar_g10', label: 'دختر ۱۰', category: 'girls', imageSrc: '/resources/avatar_g10.png' },
  { id: 'avatar_g11', label: 'دختر ۱۱', category: 'girls', imageSrc: '/resources/avatar_g11.png' },
  { id: 'avatar_g12', label: 'دختر ۱۲', category: 'girls', imageSrc: '/resources/avatar_g12.png' },

  // Boys 1-12
  { id: 'avatar_b1', label: 'پسر ۱', category: 'boys', imageSrc: '/resources/avatar_b1.png' },
  { id: 'avatar_b2', label: 'پسر ۲', category: 'boys', imageSrc: '/resources/avatar_b2.png' },
  { id: 'avatar_b3', label: 'پسر ۳', category: 'boys', imageSrc: '/resources/avatar_b3.png' },
  { id: 'avatar_b4', label: 'پسر ۴', category: 'boys', imageSrc: '/resources/avatar_b4.png' },
  { id: 'avatar_b5', label: 'پسر ۵', category: 'boys', imageSrc: '/resources/avatar_b5.png' },
  { id: 'avatar_b6', label: 'پسر ۶', category: 'boys', imageSrc: '/resources/avatar_b6.png' },
  { id: 'avatar_b7', label: 'پسر ۷', category: 'boys', imageSrc: '/resources/avatar_b7.png' },
  { id: 'avatar_b8', label: 'پسر ۸', category: 'boys', imageSrc: '/resources/avatar_b8.png' },
  { id: 'avatar_b9', label: 'پسر ۹', category: 'boys', imageSrc: '/resources/avatar_b9.png' },
  { id: 'avatar_b10', label: 'پسر ۱۰', category: 'boys', imageSrc: '/resources/avatar_b10.png' },
  { id: 'avatar_b11', label: 'پسر ۱۱', category: 'boys', imageSrc: '/resources/avatar_b11.png' },
  { id: 'avatar_b12', label: 'پسر ۱۲', category: 'boys', imageSrc: '/resources/avatar_b12.png' },
];

/**
 * Normalizes any avatar name or code into a public URL.
 */
export function getAvatarUrl(avatarKey?: string | null): string {
  if (!avatarKey) return '/resources/avatar_g1.png';

  const normalized = avatarKey.trim().toLowerCase().replace(/-/g, '_');

  // Direct match in predefined list
  const direct = PROFILE_AVATARS.find(
    (a) => a.id === normalized || a.id.replace('avatar_', '') === normalized
  );
  if (direct) return direct.imageSrc;

  // Partial match (e.g. "g7" -> "avatar_g7")
  if (/^[gb]\d+$/.test(normalized)) {
    return `/resources/avatar_${normalized}.png`;
  }

  // Already a full path or url
  if (avatarKey.startsWith('http://') || avatarKey.startsWith('https://') || avatarKey.startsWith('/')) {
    return avatarKey;
  }

  return '/resources/avatar_g1.png';
}
