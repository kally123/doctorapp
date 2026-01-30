'use client';

import React from 'react';
import Link from 'next/link';
import { Clock, Eye, Heart } from 'lucide-react';

interface Article {
  id: string;
  slug: string;
  title: string;
  excerpt: string;
  featuredImage: string;
  author: {
    name: string;
    avatar: string;
    specialization: string;
  };
  category: { id: string; name: string };
  readTimeMinutes: number;
  publishedAt: string;
  stats: { views: number; likes: number };
}

interface FeaturedArticlesProps {
  articles: Article[];
}

export default function FeaturedArticles({ articles }: FeaturedArticlesProps) {
  if (!articles || articles.length === 0) return null;

  return (
    <div className="grid md:grid-cols-2 gap-6">
      {articles.map((article, index) => (
        <Link
          key={article.id}
          href={`/articles/${article.slug}`}
          className={`group bg-white rounded-xl shadow-lg overflow-hidden hover:shadow-xl transition-shadow ${
            index === 0 ? 'md:row-span-2' : ''
          }`}
        >
          <div className="relative h-48 md:h-full min-h-[200px]">
            <img
              src={article.featuredImage}
              alt={article.title}
              className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent" />
            
            {/* Featured Badge */}
            <div className="absolute top-4 left-4">
              <span className="px-3 py-1 bg-blue-600 text-white text-xs font-medium rounded-full">
                Featured
              </span>
            </div>

            {/* Content Overlay */}
            <div className="absolute bottom-0 left-0 right-0 p-6 text-white">
              <span className="text-sm text-blue-300 mb-2 block">
                {article.category.name}
              </span>
              <h3 className={`font-bold mb-2 group-hover:text-blue-300 transition-colors ${
                index === 0 ? 'text-2xl' : 'text-xl'
              }`}>
                {article.title}
              </h3>
              <p className="text-gray-300 text-sm line-clamp-2 mb-4">
                {article.excerpt}
              </p>
              
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <img
                    src={article.author.avatar}
                    alt={article.author.name}
                    className="w-8 h-8 rounded-full border-2 border-white"
                  />
                  <div>
                    <div className="text-sm font-medium">{article.author.name}</div>
                    <div className="text-xs text-gray-400">{article.author.specialization}</div>
                  </div>
                </div>
                
                <div className="flex items-center gap-4 text-sm text-gray-300">
                  <span className="flex items-center gap-1">
                    <Clock className="w-4 h-4" />
                    {article.readTimeMinutes} min
                  </span>
                  <span className="flex items-center gap-1">
                    <Eye className="w-4 h-4" />
                    {article.stats.views.toLocaleString()}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </Link>
      ))}
    </div>
  );
}
