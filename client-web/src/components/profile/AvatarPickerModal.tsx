import React, { useState } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { Check, X } from 'lucide-react';
import { PROFILE_AVATARS, ProfileAvatarOption } from '../../utils/avatar';
import { CupertinoSegmentedControl } from '../cupertino';

interface AvatarPickerModalProps {
  isOpen: boolean;
  currentAvatar: string;
  isSaving: boolean;
  onSelect: (avatarId: string) => Promise<void>;
  onClose: () => void;
}

type AvatarCategory = 'girls' | 'boys';

export const AvatarPickerModal: React.FC<AvatarPickerModalProps> = ({
  isOpen,
  currentAvatar,
  isSaving,
  onSelect,
  onClose,
}) => {
  const [category, setCategory] = useState<AvatarCategory>('girls');
  const [selectedId, setSelectedId] = useState<string>(() => {
    const found = PROFILE_AVATARS.find(
      (a) => a.id === currentAvatar || a.imageSrc.includes(currentAvatar)
    );
    return found?.id || 'avatar_g1';
  });

  const filteredAvatars = PROFILE_AVATARS.filter((a) => a.category === category);

  const handleSave = async () => {
    await onSelect(selectedId);
    onClose();
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
          {/* Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="fixed inset-0 bg-black/60 backdrop-blur-md"
          />

          {/* Dialog Container */}
          <motion.div
            initial={{ opacity: 0, scale: 0.94, y: 40 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.94, y: 40 }}
            transition={{ type: 'spring', damping: 26, stiffness: 320 }}
            className="relative w-full max-w-md max-h-[85vh] bg-apple-surface border border-apple-border rounded-t-3xl sm:rounded-3xl shadow-2xl overflow-hidden flex flex-col z-10"
          >
            {/* Header */}
            <div className="flex items-center justify-between p-4 border-b border-white/5">
              <h3 className="text-base font-bold text-white">انتخاب تصویر پروفایل</h3>
              <button
                type="button"
                onClick={onClose}
                className="w-8 h-8 rounded-full bg-white/10 hover:bg-white/15 flex items-center justify-center text-apple-gray-300 transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Category Selector */}
            <div className="p-4 pb-2">
              <CupertinoSegmentedControl<AvatarCategory>
                value={category}
                onChange={setCategory}
                items={[
                  { id: 'girls', label: 'شخصیت‌های دخترانه' },
                  { id: 'boys', label: 'شخصیت‌های پسرانه' },
                ]}
              />
            </div>

            {/* Avatars Grid */}
            <div className="p-4 overflow-y-auto max-h-[50vh] grid grid-cols-4 gap-3">
              {filteredAvatars.map((item: ProfileAvatarOption) => {
                const isSelected = selectedId === item.id;

                return (
                  <motion.button
                    key={item.id}
                    type="button"
                    onClick={() => setSelectedId(item.id)}
                    whileHover={{ scale: 1.05 }}
                    whileTap={{ scale: 0.93 }}
                    className={`relative aspect-square rounded-2xl overflow-hidden border-2 transition-all p-1 flex items-center justify-center ${
                      isSelected
                        ? 'border-apple-blue bg-apple-blue/20 shadow-apple-glow ring-2 ring-apple-blue/50'
                        : 'border-white/10 bg-white/5 hover:border-white/20'
                    }`}
                  >
                    <img
                      src={item.imageSrc}
                      alt={item.label}
                      className="w-full h-full object-cover rounded-xl"
                    />
                    {isSelected && (
                      <div className="absolute top-1.5 right-1.5 w-5 h-5 rounded-full bg-apple-blue text-white flex items-center justify-center shadow-md">
                        <Check className="w-3 h-3 stroke-[3]" />
                      </div>
                    )}
                  </motion.button>
                );
              })}
            </div>

            {/* Footer Actions */}
            <div className="p-4 border-t border-white/5 flex items-center justify-end gap-2 bg-black/20">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 rounded-xl text-xs font-semibold text-apple-gray-300 hover:text-white transition-colors"
              >
                انصراف
              </button>
              <button
                type="button"
                disabled={isSaving}
                onClick={handleSave}
                className="px-5 py-2 rounded-xl bg-apple-blue hover:bg-blue-600 disabled:opacity-50 text-white text-xs font-semibold shadow-md active:scale-95 transition-all flex items-center gap-1.5"
              >
                {isSaving ? 'در حال ذخیره…' : 'تایید و ذخیره'}
              </button>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};
