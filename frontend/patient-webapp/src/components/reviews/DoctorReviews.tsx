'use client';

import React, { useState } from 'react';
import { Star, ThumbsUp, ThumbsDown, Flag, CheckCircle, User } from 'lucide-react';

interface Review {
  id: string;
  patientName: string;
  rating: number;
  waitTimeRating: number;
  bedsideMannerRating: number;
  explanationRating: number;
  title: string;
  reviewText: string;
  consultationType: 'VIDEO' | 'IN_PERSON' | 'AUDIO';
  isVerified: boolean;
  isAnonymous: boolean;
  positiveTags: string[];
  improvementTags: string[];
  doctorResponse?: string;
  doctorRespondedAt?: string;
  helpfulCount: number;
  notHelpfulCount: number;
  createdAt: string;
  userVote?: 'HELPFUL' | 'NOT_HELPFUL';
}

interface DoctorReviewsProps {
  doctorId: string;
  doctorName: string;
  averageRating: number;
  totalReviews: number;
  ratingDistribution: {
    fiveStar: number;
    fourStar: number;
    threeStar: number;
    twoStar: number;
    oneStar: number;
  };
  reviews: Review[];
}

const RatingBar: React.FC<{ label: string; count: number; total: number; stars: number }> = ({
  label,
  count,
  total,
  stars,
}) => {
  const percentage = total > 0 ? (count / total) * 100 : 0;
  
  return (
    <div className="flex items-center gap-2 text-sm">
      <span className="w-12 text-gray-600">{label}</span>
      <div className="flex-1 h-2 bg-gray-200 rounded-full overflow-hidden">
        <div
          className="h-full bg-yellow-400 rounded-full transition-all"
          style={{ width: `${percentage}%` }}
        />
      </div>
      <span className="w-8 text-gray-500 text-right">{count}</span>
    </div>
  );
};

const StarRating: React.FC<{ rating: number; size?: 'sm' | 'md' | 'lg' }> = ({ rating, size = 'md' }) => {
  const sizeClass = {
    sm: 'w-3 h-3',
    md: 'w-4 h-4',
    lg: 'w-5 h-5',
  }[size];

  return (
    <div className="flex items-center gap-0.5">
      {[1, 2, 3, 4, 5].map((star) => (
        <Star
          key={star}
          className={`${sizeClass} ${
            star <= rating ? 'text-yellow-400 fill-yellow-400' : 'text-gray-300'
          }`}
        />
      ))}
    </div>
  );
};

const ReviewCard: React.FC<{
  review: Review;
  onVote: (reviewId: string, voteType: 'HELPFUL' | 'NOT_HELPFUL') => void;
  onReport: (reviewId: string) => void;
}> = ({ review, onVote, onReport }) => {
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const consultationTypeLabel = {
    VIDEO: 'Video Consultation',
    IN_PERSON: 'In-Person Visit',
    AUDIO: 'Audio Consultation',
  }[review.consultationType];

  return (
    <div className="border border-gray-200 rounded-lg p-5 hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between mb-3">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
            <User className="w-5 h-5 text-blue-600" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-medium text-gray-900">
                {review.isAnonymous ? 'Anonymous Patient' : review.patientName}
              </span>
              {review.isVerified && (
                <span className="flex items-center gap-1 text-xs text-green-600 bg-green-50 px-2 py-0.5 rounded-full">
                  <CheckCircle className="w-3 h-3" />
                  Verified Patient
                </span>
              )}
            </div>
            <div className="flex items-center gap-2 text-sm text-gray-500">
              <span>{consultationTypeLabel}</span>
              <span>•</span>
              <span>{formatDate(review.createdAt)}</span>
            </div>
          </div>
        </div>
        <StarRating rating={review.rating} />
      </div>

      {review.title && (
        <h4 className="font-medium text-gray-900 mb-2">{review.title}</h4>
      )}

      <p className="text-gray-700 mb-4">{review.reviewText}</p>

      {/* Detailed Ratings */}
      <div className="grid grid-cols-3 gap-4 mb-4 p-3 bg-gray-50 rounded-lg">
        <div className="text-center">
          <div className="text-xs text-gray-500 mb-1">Wait Time</div>
          <StarRating rating={review.waitTimeRating} size="sm" />
        </div>
        <div className="text-center">
          <div className="text-xs text-gray-500 mb-1">Bedside Manner</div>
          <StarRating rating={review.bedsideMannerRating} size="sm" />
        </div>
        <div className="text-center">
          <div className="text-xs text-gray-500 mb-1">Explanation</div>
          <StarRating rating={review.explanationRating} size="sm" />
        </div>
      </div>

      {/* Tags */}
      {(review.positiveTags.length > 0 || review.improvementTags.length > 0) && (
        <div className="flex flex-wrap gap-2 mb-4">
          {review.positiveTags.map((tag) => (
            <span
              key={tag}
              className="px-2 py-1 bg-green-50 text-green-700 text-xs rounded-full"
            >
              {tag}
            </span>
          ))}
          {review.improvementTags.map((tag) => (
            <span
              key={tag}
              className="px-2 py-1 bg-orange-50 text-orange-700 text-xs rounded-full"
            >
              {tag}
            </span>
          ))}
        </div>
      )}

      {/* Doctor Response */}
      {review.doctorResponse && (
        <div className="mt-4 p-4 bg-blue-50 rounded-lg border-l-4 border-blue-500">
          <div className="flex items-center gap-2 mb-2">
            <span className="font-medium text-blue-900">Doctor's Response</span>
            {review.doctorRespondedAt && (
              <span className="text-xs text-blue-600">
                {formatDate(review.doctorRespondedAt)}
              </span>
            )}
          </div>
          <p className="text-blue-800 text-sm">{review.doctorResponse}</p>
        </div>
      )}

      {/* Actions */}
      <div className="flex items-center justify-between mt-4 pt-4 border-t">
        <div className="flex items-center gap-4">
          <span className="text-sm text-gray-500">Was this review helpful?</span>
          <button
            onClick={() => onVote(review.id, 'HELPFUL')}
            className={`flex items-center gap-1 text-sm ${
              review.userVote === 'HELPFUL'
                ? 'text-green-600'
                : 'text-gray-500 hover:text-green-600'
            }`}
          >
            <ThumbsUp className="w-4 h-4" />
            <span>{review.helpfulCount}</span>
          </button>
          <button
            onClick={() => onVote(review.id, 'NOT_HELPFUL')}
            className={`flex items-center gap-1 text-sm ${
              review.userVote === 'NOT_HELPFUL'
                ? 'text-red-600'
                : 'text-gray-500 hover:text-red-600'
            }`}
          >
            <ThumbsDown className="w-4 h-4" />
            <span>{review.notHelpfulCount}</span>
          </button>
        </div>
        <button
          onClick={() => onReport(review.id)}
          className="flex items-center gap-1 text-sm text-gray-400 hover:text-red-500"
        >
          <Flag className="w-4 h-4" />
          Report
        </button>
      </div>
    </div>
  );
};

export const DoctorReviews: React.FC<DoctorReviewsProps> = ({
  doctorId,
  doctorName,
  averageRating,
  totalReviews,
  ratingDistribution,
  reviews,
}) => {
  const [sortBy, setSortBy] = useState<'recent' | 'helpful' | 'rating'>('recent');
  const [filterRating, setFilterRating] = useState<number | null>(null);

  const handleVote = async (reviewId: string, voteType: 'HELPFUL' | 'NOT_HELPFUL') => {
    try {
      await fetch(`/api/v1/reviews/${reviewId}/vote`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ voteType }),
      });
      // Refresh reviews
    } catch (error) {
      console.error('Failed to vote:', error);
    }
  };

  const handleReport = async (reviewId: string) => {
    // Open report modal
    console.log('Report review:', reviewId);
  };

  const sortedReviews = [...reviews].sort((a, b) => {
    switch (sortBy) {
      case 'helpful':
        return b.helpfulCount - a.helpfulCount;
      case 'rating':
        return b.rating - a.rating;
      default:
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
    }
  });

  const filteredReviews = filterRating
    ? sortedReviews.filter((r) => r.rating === filterRating)
    : sortedReviews;

  return (
    <div className="bg-white rounded-xl shadow-sm">
      {/* Header with Summary */}
      <div className="p-6 border-b">
        <h2 className="text-xl font-semibold text-gray-900 mb-6">
          Patient Reviews for {doctorName}
        </h2>

        <div className="flex flex-col md:flex-row gap-8">
          {/* Overall Rating */}
          <div className="text-center md:text-left">
            <div className="text-5xl font-bold text-gray-900 mb-2">
              {averageRating.toFixed(1)}
            </div>
            <StarRating rating={Math.round(averageRating)} size="lg" />
            <div className="text-sm text-gray-500 mt-1">
              Based on {totalReviews} reviews
            </div>
          </div>

          {/* Rating Distribution */}
          <div className="flex-1 max-w-sm space-y-2">
            <RatingBar
              label="5 star"
              count={ratingDistribution.fiveStar}
              total={totalReviews}
              stars={5}
            />
            <RatingBar
              label="4 star"
              count={ratingDistribution.fourStar}
              total={totalReviews}
              stars={4}
            />
            <RatingBar
              label="3 star"
              count={ratingDistribution.threeStar}
              total={totalReviews}
              stars={3}
            />
            <RatingBar
              label="2 star"
              count={ratingDistribution.twoStar}
              total={totalReviews}
              stars={2}
            />
            <RatingBar
              label="1 star"
              count={ratingDistribution.oneStar}
              total={totalReviews}
              stars={1}
            />
          </div>
        </div>
      </div>

      {/* Filters and Sort */}
      <div className="p-4 border-b flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-2">
          <span className="text-sm text-gray-500">Filter by:</span>
          {[5, 4, 3, 2, 1].map((stars) => (
            <button
              key={stars}
              onClick={() => setFilterRating(filterRating === stars ? null : stars)}
              className={`flex items-center gap-1 px-3 py-1.5 rounded-full text-sm transition ${
                filterRating === stars
                  ? 'bg-blue-100 text-blue-700'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              {stars}
              <Star className="w-3 h-3 fill-current" />
            </button>
          ))}
        </div>

        <div className="flex items-center gap-2">
          <span className="text-sm text-gray-500">Sort by:</span>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as typeof sortBy)}
            className="border border-gray-300 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="recent">Most Recent</option>
            <option value="helpful">Most Helpful</option>
            <option value="rating">Highest Rating</option>
          </select>
        </div>
      </div>

      {/* Reviews List */}
      <div className="p-6 space-y-6">
        {filteredReviews.length > 0 ? (
          filteredReviews.map((review) => (
            <ReviewCard
              key={review.id}
              review={review}
              onVote={handleVote}
              onReport={handleReport}
            />
          ))
        ) : (
          <div className="text-center py-12 text-gray-500">
            No reviews found matching your criteria.
          </div>
        )}
      </div>
    </div>
  );
};

export default DoctorReviews;
