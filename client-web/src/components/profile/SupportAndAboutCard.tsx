import React from 'react';
import { StoryVideoCard } from './StoryVideoCard';
import { SupportCard } from './SupportCard';

export { StoryVideoCard, SupportCard };

interface SupportAndAboutCardProps {
  onSupportClick: () => void;
  onIntroVideoClick?: () => void;
}

export const SupportAndAboutCard: React.FC<SupportAndAboutCardProps> = ({
  onSupportClick,
  onIntroVideoClick,
}) => {
  return (
    <div className="space-y-3">
      {onIntroVideoClick && <StoryVideoCard onPlay={onIntroVideoClick} />}
      <SupportCard onClick={onSupportClick} />
    </div>
  );
};
