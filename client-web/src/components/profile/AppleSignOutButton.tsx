import React from 'react';
import { motion } from 'motion/react';
import { LogOut } from 'lucide-react';

interface AppleSignOutButtonProps {
  onClick: () => void;
}

export const AppleSignOutButton: React.FC<AppleSignOutButtonProps> = ({ onClick }) => {
  return (
    <motion.button
      type="button"
      onClick={onClick}
      whileHover={{ scale: 1.01 }}
      whileTap={{ scale: 0.97 }}
      transition={{ type: 'spring', damping: 20, stiffness: 350 }}
      className="w-full flex items-center justify-center gap-2.5 h-13 py-3.5 px-4 rounded-[20px] bg-red-500/10 hover:bg-red-500/15 border border-red-500/30 text-red-400 hover:text-red-300 font-bold text-sm shadow-sm backdrop-blur-xl transition-all focus:outline-none"
    >
      <LogOut className="w-4 h-4" />
      <span>خروج از حساب کاربری</span>
    </motion.button>
  );
};
