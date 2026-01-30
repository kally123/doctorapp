'use client';

import React, { useState } from 'react';
import { Star, X, Loader2 } from 'lucide-react';

interface SubmitReviewModalProps {
  isOpen: boolean;
  onClose: () => void;
  doctorId: string;
  doctorName: string;
  consultationId: string;
  consultationType: 'VIDEO' | 'IN_PERSON' | 'AUDIO';
  onSubmit: (review: ReviewData) => Promise<void>;
}

interface ReviewData {
  overallRating: number;
  waitTimeRating: number;
  bedsideMannerRating: number;
  explanationRating: number;
  title: string;
  reviewText: string;
  positiveTags: string[];
  improvementTags: string[];
  isAnonymous: boolean;
}

const POSITIVE_TAGS = [
  'Great listener',
  'Very thorough',
  'Easy to understand',
  'Friendly staff',
  'Quick diagnosis',
  'Caring',
  'Professional',
  'Punctual',
];

const IMPROVEMENT_TAGS = [
  'Long wait time',
  'Rushed appointment',
  'Billing issues',
  'Hard to reach',
  'Could explain more',
];

const StarInput: React.FC<{
  label: string;
  value: number;
  onChange: (value: number) => void;
  required?: boolean;
}> = ({ label, value, onChange, required }) => {
  const [hoverValue, setHoverValue] = useState(0);

  return (
    <div className="flex items-center justify-between">
      <span className="text-sm text-gray-700">
        {label}
        {required && <span className="text-red-500 ml-1">*</span>}
      </span>
      <div className="flex gap-1">
        {[1, 2, 3, 4, 5].map((star) => (
          <button
            key={star}
            type="button"
            onClick={() => onChange(star)}
            onMouseEnter={() => setHoverValue(star)}
            onMouseLeave={() => setHoverValue(0)}
            className="focus:outline-none transition-transform hover:scale-110"
          >
            <Star
              className={`w-6 h-6 ${
                star <= (hoverValue || value)
                  ? 'text-yellow-400 fill-yellow-400'
                  : 'text-gray-300'
              }`}
            />
          </button>
        ))}
      </div>
    </div>
  );
};

const TagSelector: React.FC<{
  label: string;
  tags: string[];
  selectedTags: string[];
  onToggle: (tag: string) => void;
  variant: 'positive' | 'improvement';
}> = ({ label, tags, selectedTags, onToggle, variant }) => {
  const colorClasses = {
    positive: {
      selected: 'bg-green-100 text-green-700 border-green-300',
      unselected: 'bg-white text-gray-600 border-gray-300 hover:border-green-300',
    },
    improvement: {
      selected: 'bg-orange-100 text-orange-700 border-orange-300',
      unselected: 'bg-white text-gray-600 border-gray-300 hover:border-orange-300',
    },
  };

  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-2">{label}</label>
      <div className="flex flex-wrap gap-2">
        {tags.map((tag) => (
          <button
            key={tag}
            type="button"
            onClick={() => onToggle(tag)}
            className={`px-3 py-1.5 rounded-full text-sm border transition ${
              selectedTags.includes(tag)
                ? colorClasses[variant].selected
                : colorClasses[variant].unselected
            }`}
          >
            {tag}
          </button>
        ))}
      </div>
    </div>
  );
};

export const SubmitReviewModal: React.FC<SubmitReviewModalProps> = ({
  isOpen,
  onClose,
  doctorId,
  doctorName,
  consultationId,
  consultationType,
  onSubmit,
}) => {
  const [formData, setFormData] = useState<ReviewData>({
    overallRating: 0,
    waitTimeRating: 0,
    bedsideMannerRating: 0,
    explanationRating: 0,
    title: '',
    reviewText: '',
    positiveTags: [],
    improvementTags: [],
    isAnonymous: false,
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (formData.overallRating === 0) {
      setError('Please select an overall rating');
      return;
    }

    if (formData.reviewText.length < 20) {
      setError('Please write at least 20 characters in your review');
      return;
    }

    setIsSubmitting(true);
    try {
      await onSubmit(formData);
      onClose();
    } catch (err) {
      setError('Failed to submit review. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const toggleTag = (tag: string, type: 'positive' | 'improvement') => {
    const field = type === 'positive' ? 'positiveTags' : 'improvementTags';
    const currentTags = formData[field];
    const newTags = currentTags.includes(tag)
      ? currentTags.filter((t) => t !== tag)
      : [...currentTags, tag];
    setFormData({ ...formData, [field]: newTags });
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex min-h-screen items-center justify-center p-4">
        {/* Backdrop */}
        <div
          className="fixed inset-0 bg-black bg-opacity-50 transition-opacity"
          onClick={onClose}
        />

        {/* Modal */}
        <div className="relative bg-white rounded-xl shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
          {/* Header */}
          <div className="sticky top-0 bg-white border-b px-6 py-4 flex items-center justify-between">
            <div>
              <h2 className="text-xl font-semibold text-gray-900">Write a Review</h2>
              <p className="text-sm text-gray-500">Share your experience with {doctorName}</p>
            </div>
            <button
              onClick={onClose}
              className="p-2 hover:bg-gray-100 rounded-full transition"
            >
              <X className="w-5 h-5 text-gray-500" />
            </button>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="p-6 space-y-6">
            {error && (
              <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
                {error}
              </div>
            )}

            {/* Consultation Type Badge */}
            <div className="flex items-center gap-2">
              <span className="text-sm text-gray-500">Reviewing:</span>
              <span className="px-3 py-1 bg-blue-50 text-blue-700 rounded-full text-sm">
                {consultationType === 'VIDEO'
                  ? '📹 Video Consultation'
                  : consultationType === 'AUDIO'
                  ? '📞 Audio Consultation'
                  : '🏥 In-Person Visit'}
              </span>
            </div>

            {/* Ratings */}
            <div className="space-y-4 p-4 bg-gray-50 rounded-lg">
              <StarInput
                label="Overall Rating"
                value={formData.overallRating}
                onChange={(value) => setFormData({ ...formData, overallRating: value })}
                required
              />
              <StarInput
                label="Wait Time"
                value={formData.waitTimeRating}
                onChange={(value) => setFormData({ ...formData, waitTimeRating: value })}
              />
              <StarInput
                label="Bedside Manner"
                value={formData.bedsideMannerRating}
                onChange={(value) => setFormData({ ...formData, bedsideMannerRating: value })}
              />
              <StarInput
                label="Explanation of Condition"
                value={formData.explanationRating}
                onChange={(value) => setFormData({ ...formData, explanationRating: value })}
              />
            </div>

            {/* Title */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Review Title (optional)
              </label>
              <input
                type="text"
                value={formData.title}
                onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                placeholder="Summarize your experience..."
                maxLength={200}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            {/* Review Text */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Your Review <span className="text-red-500">*</span>
              </label>
              <textarea
                value={formData.reviewText}
                onChange={(e) => setFormData({ ...formData, reviewText: e.target.value })}
                placeholder="Share details of your experience with this doctor..."
                rows={5}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
              />
              <div className="text-right text-xs text-gray-400 mt-1">
                {formData.reviewText.length} / 2000 characters
              </div>
            </div>

            {/* Positive Tags */}
            <TagSelector
              label="What did you like? (optional)"
              tags={POSITIVE_TAGS}
              selectedTags={formData.positiveTags}
              onToggle={(tag) => toggleTag(tag, 'positive')}
              variant="positive"
            />

            {/* Improvement Tags */}
            <TagSelector
              label="What could be improved? (optional)"
              tags={IMPROVEMENT_TAGS}
              selectedTags={formData.improvementTags}
              onToggle={(tag) => toggleTag(tag, 'improvement')}
              variant="improvement"
            />

            {/* Anonymous Toggle */}
            <label className="flex items-center gap-3 cursor-pointer">
              <input
                type="checkbox"
                checked={formData.isAnonymous}
                onChange={(e) => setFormData({ ...formData, isAnonymous: e.target.checked })}
                className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
              />
              <span className="text-sm text-gray-700">
                Post anonymously (your name won't be shown)
              </span>
            </label>

            {/* Submit */}
            <div className="flex justify-end gap-3 pt-4 border-t">
              <button
                type="button"
                onClick={onClose}
                className="px-6 py-2 text-gray-700 hover:bg-gray-100 rounded-lg transition"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={isSubmitting}
                className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
              >
                {isSubmitting ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    Submitting...
                  </>
                ) : (
                  'Submit Review'
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default SubmitReviewModal;
