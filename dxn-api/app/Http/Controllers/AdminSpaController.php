<?php

namespace App\Http\Controllers;

use Symfony\Component\HttpFoundation\BinaryFileResponse;

class AdminSpaController extends Controller
{
    /**
     * Serve the built React entry point for every /admin/... route.
     * The React BrowserRouter handles the path client-side.
     */
    public function __invoke(): BinaryFileResponse
    {
        return response()->file(public_path('admin/index.html'));
    }
}
