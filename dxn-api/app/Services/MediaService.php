<?php

namespace App\Services;

use App\Models\LessonCard;
use Illuminate\Support\Facades\Storage;

class MediaService
{
    /**
     * Delete a stored public-disk file when no lesson card references it anymore.
     */
    public function deleteIfUnreferenced(?string $url): void
    {
        $path = $this->pathFromUrl($url);
        if ($path === null) {
            return;
        }

        if (LessonCard::where('media_url', $url)->exists()) {
            return;
        }

        Storage::disk('public')->delete($path);
    }

    /**
     * Map a public-disk URL back to a media/ path. Anything outside media/ is ignored.
     */
    public function pathFromUrl(?string $url): ?string
    {
        if ($url === null || $url === '') {
            return null;
        }

        $marker = '/storage/';
        $position = strpos($url, $marker);
        if ($position === false) {
            return null;
        }

        $path = ltrim(substr($url, $position + strlen($marker)), '/');

        return str_starts_with($path, 'media/') ? $path : null;
    }
}
