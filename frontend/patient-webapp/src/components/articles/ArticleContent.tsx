'use client';

import React, { useState } from 'react';
import ReactMarkdown from 'react-markdown';
import { Heart, Bookmark, Share2, MessageCircle, ThumbsUp, CheckCircle } from 'lucide-react';

interface Article {
  id: string;
  content: string;
  stats: {
    views: number;
    likes: number;
    bookmarks: number;
    comments: number;
  };
  medicalReview?: {
    reviewedBy: string;
    reviewedAt: string;
  };
}

interface ArticleContentProps {
  article: Article;
}

export default function ArticleContent({ article }: ArticleContentProps) {
  const [isLiked, setIsLiked] = useState(false);
  const [isBookmarked, setIsBookmarked] = useState(false);
  const [likeCount, setLikeCount] = useState(article.stats.likes);

  const handleLike = async () => {
    try {
      // await fetch(`/api/v1/articles/${article.id}/like`, { method: 'POST' });
      setIsLiked(!isLiked);
      setLikeCount(isLiked ? likeCount - 1 : likeCount + 1);
    } catch (error) {
      console.error('Failed to like article:', error);
    }
  };

  const handleBookmark = async () => {
    try {
      // await fetch(`/api/v1/articles/${article.id}/bookmark`, { method: 'POST' });
      setIsBookmarked(!isBookmarked);
    } catch (error) {
      console.error('Failed to bookmark article:', error);
    }
  };

  const handleShare = async () => {
    if (navigator.share) {
      try {
        await navigator.share({
          title: document.title,
          url: window.location.href,
        });
      } catch (error) {
        console.error('Failed to share:', error);
      }
    } else {
      navigator.clipboard.writeText(window.location.href);
      alert('Link copied to clipboard!');
    }
  };

  return (
    <div className="bg-white rounded-xl shadow-sm">
      {/* Medical Review Badge */}
      {article.medicalReview && (
        <div className="px-6 py-4 bg-green-50 border-b border-green-100 rounded-t-xl">
          <div className="flex items-center gap-2 text-green-800">
            <CheckCircle className="w-5 h-5" />
            <span className="font-medium">Medically Reviewed</span>
          </div>
          <p className="text-sm text-green-700 mt-1">
            Reviewed by {article.medicalReview.reviewedBy} on{' '}
            {new Date(article.medicalReview.reviewedAt).toLocaleDateString('en-US', {
              year: 'numeric',
              month: 'long',
              day: 'numeric',
            })}
          </p>
        </div>
      )}

      {/* Article Content */}
      <div className="px-6 py-8">
        <article className="prose prose-lg max-w-none prose-headings:font-semibold prose-headings:text-gray-900 prose-p:text-gray-700 prose-a:text-blue-600 prose-strong:text-gray-900 prose-ul:text-gray-700 prose-ol:text-gray-700">
          <ReactMarkdown>{article.content}</ReactMarkdown>
        </article>
      </div>

      {/* Engagement Actions */}
      <div className="px-6 py-4 border-t flex items-center justify-between">
        <div className="flex items-center gap-4">
          <button
            onClick={handleLike}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg transition ${
              isLiked
                ? 'bg-red-50 text-red-600'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            <Heart className={`w-5 h-5 ${isLiked ? 'fill-current' : ''}`} />
            <span>{likeCount}</span>
          </button>

          <button
            onClick={handleBookmark}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg transition ${
              isBookmarked
                ? 'bg-blue-50 text-blue-600'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            <Bookmark className={`w-5 h-5 ${isBookmarked ? 'fill-current' : ''}`} />
            <span>{isBookmarked ? 'Saved' : 'Save'}</span>
          </button>

          <button
            onClick={handleShare}
            className="flex items-center gap-2 px-4 py-2 rounded-lg bg-gray-100 text-gray-600 hover:bg-gray-200 transition"
          >
            <Share2 className="w-5 h-5" />
            <span>Share</span>
          </button>
        </div>

        <div className="flex items-center gap-2 text-gray-500">
          <MessageCircle className="w-5 h-5" />
          <span>{article.stats.comments} comments</span>
        </div>
      </div>

      {/* Disclaimer */}
      <div className="px-6 py-4 bg-gray-50 border-t rounded-b-xl">
        <p className="text-sm text-gray-500">
          <strong>Disclaimer:</strong> This article is for informational purposes only and should not replace
          professional medical advice, diagnosis, or treatment. Always seek the advice of your physician
          or other qualified health provider with any questions you may have regarding a medical condition.
        </p>
      </div>
    </div>
  );
}
