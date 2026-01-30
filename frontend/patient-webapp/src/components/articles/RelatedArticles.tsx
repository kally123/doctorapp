'use client';

import React from 'react';
import Link from 'next/link';
import { Clock } from 'lucide-react';

interface RelatedArticle {
  id: string;
  slug: string;
  title: string;
  excerpt: string;
  featuredImage: string;
  readTimeMinutes: number;
  category: { name: string };
}

interface RelatedArticlesProps {
  articles: RelatedArticle[];
}

export default function RelatedArticles({ articles }: RelatedArticlesProps) {
  if (!articles || articles.length === 0) return null;

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-900 mb-6">Related Articles</h2>
      <div className="grid md:grid-cols-3 gap-6">
        {articles.map((article) => (
          <Link
            key={article.id}
            href={`/articles/${article.slug}`}
            className="group bg-white rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-shadow"
          >
            <div className="relative h-40">
              <img
                src={article.featuredImage}
                alt={article.title}
                className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
              />
              <div className="absolute top-3 left-3">
                <span className="px-2 py-1 bg-white/90 text-gray-700 text-xs rounded-full">
                  {article.category.name}
                </span>
              </div>
            </div>
            <div className="p-4">
              <h3 className="font-semibold text-gray-900 group-hover:text-blue-600 transition line-clamp-2 mb-2">
                {article.title}
              </h3>
              <p className="text-sm text-gray-500 line-clamp-2 mb-3">
                {article.excerpt}
              </p>
              <div className="flex items-center text-sm text-gray-400">
                <Clock className="w-4 h-4 mr-1" />
                {article.readTimeMinutes} min read
              </div>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}
