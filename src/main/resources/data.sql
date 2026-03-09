-- Schema for Content Metadata (Roku-compliant)
CREATE TABLE IF NOT EXISTS content_metadata (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content_id VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    long_description VARCHAR(2000) NOT NULL,
    stream_url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500) NOT NULL,
    sd_thumbnail_url VARCHAR(500),
    media_type VARCHAR(20) NOT NULL,
    release_date TIMESTAMP NOT NULL,
    genre VARCHAR(50),
    language VARCHAR(10),
    duration_minutes INT,
    rating VARCHAR(10)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_content_id ON content_metadata(content_id);
CREATE INDEX IF NOT EXISTS idx_media_type ON content_metadata(media_type);
CREATE INDEX IF NOT EXISTS idx_genre ON content_metadata(genre);
CREATE INDEX IF NOT EXISTS idx_language ON content_metadata(language);

-- Insert 5 sample content items with diverse metadata
INSERT INTO content_metadata (content_id, title, long_description, stream_url, thumbnail_url, sd_thumbnail_url, media_type, release_date, genre, language, duration_minutes, rating) VALUES
('movie-001', 'The Matrix Reloaded', 'Neo and the rebel leaders estimate that they have 72 hours until Zion falls under siege to the Machine Army. Only a matter of hours separates the last human enclave on Earth from 250,000 Sentinels programmed to destroy mankind. But the citizens of Zion, emboldened by Morpheus conviction that the One will fulfill the Oracles Prophecy and end the war with the Machines, rest all manner of hope and expectation on Neo, who finds himself stalled by disturbing visions as he searches for a course of action.', 
'https://content.roku.com/streams/matrix-reloaded.mp4', 
'https://images.roku.com/matrix-reloaded-hd.jpg', 
'https://images.roku.com/matrix-reloaded-sd.jpg', 
'MOVIE', 
'2003-05-15T00:00:00', 
'Action', 
'en', 
138, 
'R');

INSERT INTO content_metadata (content_id, title, long_description, stream_url, thumbnail_url, sd_thumbnail_url, media_type, release_date, genre, language, duration_minutes, rating) VALUES
('movie-002', 'Inception', 'Dom Cobb is a skilled thief, the absolute best in the dangerous art of extraction, stealing valuable secrets from deep within the subconscious during the dream state when the mind is at its most vulnerable. Cobbs rare ability has made him a coveted player in this treacherous new world of corporate espionage, but it has also made him an international fugitive and cost him everything he has ever loved. Now Cobb is being offered a chance at redemption. One last job could give him his life back but only if he can accomplish the impossible: inception.',
'https://content.roku.com/streams/inception.mp4',
'https://images.roku.com/inception-hd.jpg',
'https://images.roku.com/inception-sd.jpg',
'MOVIE',
'2010-07-16T00:00:00',
'Action',
'en',
148,
'PG-13');

INSERT INTO content_metadata (content_id, title, long_description, stream_url, thumbnail_url, sd_thumbnail_url, media_type, release_date, genre, language, duration_minutes, rating) VALUES
('series-001', 'Stranger Things: Season 1', 'When a young boy disappears, his mother, a police chief and his friends must confront terrifying supernatural forces in order to get him back. Set in 1980s Indiana, a group of young friends witness supernatural forces and secret government exploits. As they search for answers, the children unravel a series of extraordinary mysteries involving secret government experiments, terrifying supernatural forces, and one very strange little girl.',
'https://content.roku.com/streams/stranger-things-s1.mp4',
'https://images.roku.com/stranger-things-hd.jpg',
'https://images.roku.com/stranger-things-sd.jpg',
'SERIES',
'2016-07-15T00:00:00',
'Drama',
'en',
480,
'TV-14');

INSERT INTO content_metadata (content_id, title, long_description, stream_url, thumbnail_url, sd_thumbnail_url, media_type, release_date, genre, language, duration_minutes, rating) VALUES
('movie-003', 'Coco', 'Despite his familys baffling generations-old ban on music, Miguel dreams of becoming an accomplished musician like his idol, Ernesto de la Cruz. Desperate to prove his talent, Miguel finds himself in the stunning and colorful Land of the Dead following a mysterious chain of events. Along the way, he meets charming trickster Hector, and together, they set off on an extraordinary journey to unlock the real story behind Miguels family history.',
'https://content.roku.com/streams/coco.mp4',
'https://images.roku.com/coco-hd.jpg',
'https://images.roku.com/coco-sd.jpg',
'MOVIE',
'2017-11-22T00:00:00',
'Comedy',
'es',
105,
'PG');

INSERT INTO content_metadata (content_id, title, long_description, stream_url, thumbnail_url, sd_thumbnail_url, media_type, release_date, genre, language, duration_minutes, rating) VALUES
('shortform-001', 'Tech Talk: AI Revolution', 'Join industry experts as they explore the latest breakthroughs in artificial intelligence and machine learning. This episode dives deep into how AI is transforming industries from healthcare to entertainment, featuring live demos and expert interviews. Learn about the cutting-edge technologies that are shaping our future and discover practical applications you can implement today.',
'https://content.roku.com/streams/tech-talk-ai.mp4',
'https://images.roku.com/tech-talk-hd.jpg',
'https://images.roku.com/tech-talk-sd.jpg',
'SHORTFORM',
'2024-01-10T00:00:00',
'Documentary',
'en',
15,
'NR');
