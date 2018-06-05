<?php

use Faker\Factory as Faker;
use App\Models\Food;
use App\Repositories\FoodRepository;

trait MakeFoodTrait
{
    /**
     * Create fake instance of Food and save it in database
     *
     * @param array $foodFields
     * @return Food
     */
    public function makeFood($foodFields = [])
    {
        /** @var FoodRepository $foodRepo */
        $foodRepo = App::make(FoodRepository::class);
        $theme = $this->fakeFoodData($foodFields);
        return $foodRepo->create($theme);
    }

    /**
     * Get fake instance of Food
     *
     * @param array $foodFields
     * @return Food
     */
    public function fakeFood($foodFields = [])
    {
        return new Food($this->fakeFoodData($foodFields));
    }

    /**
     * Get fake data of Food
     *
     * @param array $postFields
     * @return array
     */
    public function fakeFoodData($foodFields = [])
    {
        $fake = Faker::create();

        return array_merge([
            'id' => $fake->randomDigitNotNull,
            'name' => $fake->word,
            'count' => $fake->randomDigitNotNull,
            'count_type' => $fake->randomDigitNotNull,
            'production_date' => $fake->word,
            'expiration_date' => $fake->word,
            'note' => $fake->word,
            'img1' => $fake->word,
            'img2' => $fake->word,
            'img3' => $fake->word,
            'barcode' => $fake->word,
            'created_at' => $fake->word,
            'updated_at' => $fake->word
        ], $foodFields);
    }
}
