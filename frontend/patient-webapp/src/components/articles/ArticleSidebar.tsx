'use client';

import React from 'react';
import Link from 'next/link';
import { Bookmark, BookmarkCheck, Share2, Printer, Facebook, Twitter, Linkedin } from 'lucide-react';

interface Article {
  id: string;
  author: {
    doctorId?: string;
    name: string;
    avatar: string;
    specialization: string;
    credentials?: string;
  };
  category: { id: string; name: string };
  tags: string[];
}

interface ArticleSidebarProps {
  article: Article;
}

export default function ArticleSidebar({ article }: ArticleSidebarProps) {
  const [isBookmarked, setIsBookmarked] = React.useState(false);

  const handlePrint = () => {
    window.print();
  };

  const shareUrl = typeof window !== 'undefined' ? window.location.href : '';
  const shareTitle = typeof document !== 'undefined' ? document.title : '';

  return (
    <div className="space-y-6 sticky top-24">
      {/* Author Card */}
      <div className="bg-white rounded-xl shadow-sm p-5">
        <h3 className="font-semibold text-gray-900 mb-4">About the Author</h3>
        <div className="flex items-center gap-3 mb-4">
          <img
            src={article.author.avatar || '/default-avatar.png'}
            alt={article.author.name}
            className="w-12 h-12 rounded-full"
          />
          <div>
            <div className="font-medium text-gray-900">{article.author.name}</div>
            <div className="text-sm text-gray-500">{article.author.specialization}</div>
            {article.author.credentials && (
              <div className="text-xs text-gray-400">{article.author.credentials}</div>
            )}
          </div>
        </div>
        {article.author.doctorId && (
          <Link
            href={`/doctors/${article.author.doctorId}`}
            className="block w-full text-center py-2 border border-blue-600 text-blue-600 rounded-lg hover:bg-blue-50 transition text-sm font-medium"
          >
            View Profile
          </Link>
        )}
      </div>

      {/* Actions */}
      <div className="bg-white rounded-xl shadow-sm p-5">
        <h3 className="font-semibold text-gray-900 mb-4">Actions</h3>
        <div className="space-y-2">
          <button
            onClick={() => setIsBookmarked(!isBookmarked)}
            className={`w-full flex items-center gap-3 px-4 py-2 rounded-lg transition ${
              isBookmarked
                ? 'bg-blue-50 text-blue-600'
                : 'hover:bg-gray-50 text-gray-700'
            }`}
          >
            {isBookmarked ? (
              <BookmarkCheck className="w-5 h-5" />
            ) : (
              <Bookmark className="w-5 h-5" />
            )}
            <span>{isBookmarked ? 'Saved' : 'Save for Later'}</span>
          </button>
          <button
            onClick={handlePrint}
            className="w-full flex items-center gap-3 px-4 py-2 rounded-lg hover:bg-gray-50 text-gray-700 transition"
          >
            <Printer className="w-5 h-5" />
            <span>Print Article</span>
          </button>
        </div>
      </div>

      {/* Share */}
      <div className="bg-white rounded-xl shadow-sm p-5">
        <h3 className="font-semibold text-gray-900 mb-4">Share This Article</h3>
        <div className="flex gap-2">
          <a
            href={`https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(shareUrl)}`}
            target="_blank"
            rel="noopener noreferrer"
            className="flex-1 flex items-center justify-center gap-2 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
          >
            <Facebook className="w-4 h-4" />
          </a>
          <a
            href={`https://twitter.com/intent/tweet?url=${encodeURIComponent(shareUrl)}&text=${encodeURIComponent(shareTitle)}`}
            target="_blank"
            rel="noopener noreferrer"
            className="flex-1 flex items-center justify-center gap-2 py-2 bg-sky-500 text-white rounded-lg hover:bg-sky-600 transition"
          >
            <Twitter className="w-4 h-4" />
          </a>
          <a
            href={`https://www.linkedin.com/shareArticle?mini=true&url=${encodeURIComponent(shareUrl)}&title=${encodeURIComponent(shareTitle)}`}
            target="_blank"
            rel="noopener noreferrer"
            className="flex-1 flex items-center justify-center gap-2 py-2 bg-blue-700 text-white rounded-lg hover:bg-blue-800 transition"
          >
            <Linkedin className="w-4 h-4" />
          </a>
        </div>
      </div>

      {/* Tags */}
      <div className="bg-white rounded-xl shadow-sm p-5">
        <h3 className="font-semibold text-gray-900 mb-4">Tags</h3>
        <div className="flex flex-wrap gap-2">
          {article.tags.map((tag) => (
            <Link
              key={tag}
              href={`/articles?tag=${tag}`}
              className="px-3 py-1 bg-gray-100 text-gray-600 text-sm rounded-full hover:bg-gray-200 transition"
            >
              #{tag}
            </Link>
          ))}
        </div>
      </div>

      {/* Category */}
      <div className="bg-gradient-to-br from-blue-500 to-blue-600 rounded-xl p-5 text-white">
        <h3 className="font-semibold mb-2">More in {article.category.name}</h3>
        <p className="text-sm text-blue-100 mb-4">
          Explore more articles in this category
        </p>
        <Link
          href={`/articles?category=${article.category.id}`}
          className="block w-full text-center py-2 bg-white text-blue-600 rounded-lg font-medium hover:bg-blue-50 transition"
        >
          View All
        </Link>
      </div>
    </div>
  );
}
