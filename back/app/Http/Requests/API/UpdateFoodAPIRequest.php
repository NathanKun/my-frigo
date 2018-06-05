<?php

namespace App\Http\Requests\API;

use App\Models\Food;
use InfyOm\Generator\Request\APIRequest;

class UpdateFoodAPIRequest extends APIRequest
{
    /**
     * Determine if the user is authorized to make this request.
     *
     * @return bool
     */
    public function authorize()
    {
        $authHeader = $this->header('Authorization', '');
        if ($authHeader === env('AUTH_SECRET')) {
            return true;
        }

        return false;
    }

    /**
     * Get the validation rules that apply to the request.
     *
     * @return array
     */
    public function rules()
    {
        return Food::$rules;
    }
}
