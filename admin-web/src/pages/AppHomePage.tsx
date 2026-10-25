import React from 'react';
import { LandingNavbar } from '../components/landing/LandingNavbar';
import { LandingHero } from '../components/landing/LandingHero';
import { LandingStats } from '../components/landing/LandingStats';
import { LandingFeatures } from '../components/landing/LandingFeatures';
import { LandingScenarios } from '../components/landing/LandingScenarios';
import { LandingVoiceLab } from '../components/landing/LandingVoiceLab';
import { LandingHowItWorks } from '../components/landing/LandingHowItWorks';
import { LandingPricing } from '../components/landing/LandingPricing';
import { LandingFaq } from '../components/landing/LandingFaq';
import { LandingFooter } from '../components/landing/LandingFooter';

export const AppHomePage: React.FC = () => {
  const handleLaunchApp = () => {
    window.location.href = '/app/';
  };

  return (
    <div className="min-h-screen bg-[#0B0F17] text-slate-100 flex flex-col selection:bg-cyan-500 selection:text-black relative overflow-x-hidden font-sans">
      <LandingNavbar onLaunchApp={handleLaunchApp} />
      <LandingHero onLaunchApp={handleLaunchApp} />
      <LandingStats />
      <LandingFeatures />
      <LandingScenarios onLaunchApp={handleLaunchApp} />
      <LandingVoiceLab />
      <LandingHowItWorks />
      <LandingPricing onLaunchApp={handleLaunchApp} />
      <LandingFaq />
      <LandingFooter onLaunchApp={handleLaunchApp} />
    </div>
  );
};

export default AppHomePage;
