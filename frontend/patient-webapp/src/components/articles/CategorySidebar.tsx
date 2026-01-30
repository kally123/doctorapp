'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname, useSearchParams } from 'next/navigation';
import { Heart, Brain, Activity, Apple, Dumbbell, Baby, Shield, ChevronRight } from 'lucide-react';

interface Category {
  id: string;
  name: string;
  slug: string;
  icon: string;
  articleCount: number;
}

interface CategorySidebarProps {
  categories: Category[];
}

const iconMap: Record<string, React.ReactNode> = {
  heart: <Heart className="w-5 h-5" />,
  brain: <Brain className="w-5 h-5" />,
  activity: <Activity className="w-5 h-5" />,
  apple: <Apple className="w-5 h-5" />,
  dumbbell: <Dumbbell className="w-5 h-5" />,
  baby: <Baby className="w-5 h-5" />,
  shield: <Shield className="w-5 h-5" />,
};

export default function CategorySidebar({ categories }: CategorySidebarProps) {
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const currentCategory = searchParams.get('category');

  return (
    <div className="bg-white rounded-xl shadow-sm p-5 sticky top-24">
      <h3 className="font-semibold text-gray-900 mb-4">Categories</h3>
      
      <nav className="space-y-1">
        <Link
          href="/articles"
          className={`flex items-center justify-between px-3 py-2 rounded-lg transition ${
            !currentCategory
              ? 'bg-blue-50 text-blue-700'
              : 'text-gray-700 hover:bg-gray-50'
          }`}
        >
          <span className="font-medium">All Articles</span>
          <ChevronRight className="w-4 h-4" />
        </Link>

        {categories.map((category) => (
          <Link
            key={category.id}
            href={`/articles?category=${category.slug}`}
            className={`flex items-center justify-between px-3 py-2 rounded-lg transition ${
              currentCategory === category.slug
                ? 'bg-blue-50 text-blue-700'
                : 'text-gray-700 hover:bg-gray-50'
            }`}
          >
            <div className="flex items-center gap-3">
              <span className="text-gray-400">
                {iconMap[category.icon] || <Activity className="w-5 h-5" />}
              </span>
              <span className="font-medium">{category.name}</span>
            </div>
            <span className="text-sm text-gray-400">{category.articleCount}</span>
          </Link>
        ))}
      </nav>

      {/* Popular Tags */}
      <div className="mt-8 pt-6 border-t">
        <h3 className="font-semibold text-gray-900 mb-4">Popular Tags</h3>
        <div className="flex flex-wrap gap-2">
          {['prevention', 'nutrition', 'exercise', 'mental-health', 'sleep', 'wellness'].map(
            (tag) => (
              <Link
                key={tag}
                href={`/articles?tag=${tag}`}
                className="px-3 py-1 bg-gray-100 text-gray-600 text-sm rounded-full hover:bg-gray-200 transition"
              >
                #{tag}
              </Link>
            )
          )}
        </div>
      </div>

      {/* Newsletter CTA */}
      <div className="mt-8 p-4 bg-gradient-to-br from-blue-500 to-blue-600 rounded-lg text-white">
        <h4 className="font-semibold mb-2">Health Newsletter</h4>
        <p className="text-sm text-blue-100 mb-3">
          Get the latest health tips delivered to your inbox.
        </p>
        <input
          type="email"
          placeholder="Your email"
          className="w-full px-3 py-2 rounded-lg text-gray-900 text-sm placeholder-gray-400 mb-2"
        />
        <button className="w-full py-2 bg-white text-blue-600 font-medium rounded-lg text-sm hover:bg-blue-50 transition">
          Subscribe
        </button>
      </div>
    </div>
  );
}
